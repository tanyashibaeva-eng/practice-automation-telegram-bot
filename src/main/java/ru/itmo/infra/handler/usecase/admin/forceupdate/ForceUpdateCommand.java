package ru.itmo.infra.handler.usecase.admin.forceupdate;

import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.ForceUpdateDTO;
import ru.itmo.domain.model.Student;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

import java.util.Set;

/**
 * Команда для принудительного обновления данных студента в обход валидаций.
 * <p>
 * Позволяет изменить следующие данные студента:
 * <ul>
 *     <li>Статус практики</li>
 *     <li>Место прохождения практики</li>
 *     <li>Формат практики</li>
 *     <li>ИНН компании</li>
 *     <li>Название компании</li>
 *     <li>ФИО руководителя</li>
 *     <li>Телефон руководителя</li>
 *     <li>Email руководителя</li>
 *     <li>Должность руководителя</li>
 * </ul>
 * <p>
 * Поддерживаемые ключи:
 * <ul>
 *     <li>{@code --help} - показать доступные поля для обновления</li>
 *     <li>{@code --dry-run} - режим предпросмотра без применения изменений</li>
 * </ul>
 * <p>
 * Примеры использования:
 * <ul>
 *     <li>{@code /forceupdate 123456 "Поток" status="PRACTICE_APPROVED"} - изменить статус</li>
 *     <li>{@code /forceupdate 123456 "Поток" place="ITMO_COMPANY" format="OFFLINE"} - изменить место и формат</li>
 *     <li>{@code /forceupdate --dry-run 123456 "Поток" status="PRACTICE_APPROVED"} - предпросмотр изменений</li>
 *     <li>{@code /forceupdate --help} - показать доступные поля</li>
 * </ul>
 * <p>
 * Процесс выполнения команды:
 * <ol>
 *     <li>Парсинг команды через {@link ForceUpdateCommandParser}</li>
 *     <li>Если указан {@code --help} - возврат списка доступных полей</li>
 *     <li>Если не указано ни одного поля - ошибка</li>
 *     <li>Если указан {@code --dry-run} - показ изменений без применения</li>
 *     <li>Поиск студента по chatId и имени потока</li>
 *     <li>Формирование текста подтверждения с текущими и новыми значениями</li>
 *     <li>Ожидание подтверждения от администратора ({@link ForceUpdateConfirmationCommand})</li>
 * </ol>
 *
 * @see ForceUpdateCommandParser
 * @see ForceUpdateConfirmationCommand
 * @see ForceUpdateField
 */
@Log
public class ForceUpdateCommand implements AdminCommand {

    /**
     * Множество допустимых имен полей (используется для обратной совместимости).
     * @deprecated используется {@link ForceUpdateField} для определения полей
     */
    @Deprecated
    private final Set<String> fieldNames = Set.of(
            "статус",
            "место_практики",
            "формат_практики",
            "инн_компании",
            "имя_компании",
            "фио_руководителя",
            "телефон_руководителя",
            "почта_руководителя",
            "должность_руководителя"
    );

    /**
     * Основной метод выполнения команды.
     * <p>
     * Выполняет:
     * <ul>
     *     <li>Парсинг входящего сообщения</li>
     *     <li>Проверку на ключ --help</li>
     *     <li>Валидацию наличия полей для обновления</li>
     *     <li>Обработку режима dry-run</li>
     *     <li>Поиск студента в базе данных</li>
     *     <li>Формирование текста подтверждения</li>
     * </ul>
     *
     * @param message входящее сообщение от пользователя
     * @return MessageToUser с текстом ответа и клавиатурой подтверждения
     */
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            ForceUpdateCommandParser parser = ForceUpdateCommandParser.parse(message.getText());

            if (parser.isShowFields()) {
                return MessageToUser.builder()
                        .text("Доступные поля для обновления:\n\n" + ForceUpdateField.getAvailableFieldsList())
                        .keyboardMarkup(new ReplyKeyboardRemove(true))
                        .build();
            }

            if (!parser.hasFieldsToUpdate()) {
                throw new BadRequestException(
                        "Не указано ни одного поля для обновления.\n" +
                        "Пример: /forceupdate 123456 \"Поток\" status=\"PRACTICE_APPROVED\""
                );
            }

            if (parser.isDryRun()) {
                return handleDryRun(parser);
            }

    Student student = findStudent(parser.getStudentIsu(), parser.getEduStreamName());
            ForceUpdateDTO dto = parser.toDTO();

            String text = buildConfirmationText(student, dto);

            ContextHolder.setCommandData(message.getChatId(), dto);
            ContextHolder.setNextCommand(message.getChatId(), new ForceUpdateConfirmationCommand());

            return MessageToUser.builder()
                    .text(text)
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .keyboardMarkup(getInlineKeyboard())
                    .build();

        } catch (BadRequestException e) {
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text(e.getMessage())
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .build();
        }
    }

    /**
     * Находит студента по ISU и имени потока.
     *
     * @param isu          ISU студента
     * @param eduStreamName имя потока
     * @return найденный студент
     * @throws BadRequestException если студент не найден
     * @throws InternalException при ошибках работы с базой данных
     */
    private Student findStudent(int isu, String eduStreamName) throws BadRequestException, InternalException {
        var studentOpt = StudentService.findStudentByIsuAndEduStreamName(isu, eduStreamName);
        if (studentOpt.isEmpty()) {
            throw new BadRequestException("Студент с isu %d на потоке %s не найден".formatted(isu, eduStreamName));
        }
        return studentOpt.get();
    }

    /**
     * Обрабатывает режим предпросмотра (dry-run).
     * <p>
     * Показывает какие изменения будут применены, но не сохраняет их в базу данных.
     *
     * @param parser распарсенные данные команды
     * @return MessageToUser с описанием планируемых изменений
     */
    private MessageToUser handleDryRun(ForceUpdateCommandParser parser) throws BadRequestException, InternalException {
        Student student = findStudent(parser.getStudentIsu(), parser.getEduStreamName());
        ForceUpdateDTO dto = parser.toDTO();

        StringBuilder sb = new StringBuilder();
        sb.append("=== DRY-RUN MODE ===\n\n");
        sb.append("Студент: ").append(student.getFullName()).append("\n");
        sb.append("ISU: ").append(parser.getStudentIsu()).append("\n");
        sb.append("Поток: ").append(parser.getEduStreamName()).append("\n\n");

        sb.append("Изменения:\n");
        sb.append(buildChangesList(dto, student));

        sb.append("\nДанные не изменены (dry-run).\n");
        sb.append("Для применения запустите без --dry-run.");

        return MessageToUser.builder()
                .text(sb.toString())
                .keyboardMarkup(new ReplyKeyboardRemove(true))
                .build();
    }

    /**
     * Формирует текст подтверждения с текущими и новыми значениями.
     *
     * @param student текущий студент из базы данных
     * @param dto     DTO с новыми значениями
     * @return форматированный текст подтверждения
     */
    private String buildConfirmationText(Student student, ForceUpdateDTO dto) {
        StringBuilder sb = new StringBuilder();

        sb.append("Обновление студента ").append(student.getFullName()).append("\n");
        sb.append("ISU: ").append(dto.getIsu()).append("\n");
        sb.append("Поток: ").append(student.getEduStream().getName()).append("\n\n");

        sb.append("Изменения:\n");
        sb.append(buildChangesList(dto, student));

        sb.append("\nВНИМАНИЕ: обход валидаций! Продолжить?");

        return sb.toString();
    }

    /**
     * Формирует список изменений в читабельном формате.
     *
     * @param dto     DTO с новыми значениями
     * @param student студент с текущими значениями
     * @return форматированный список изменений
     */
    private String buildChangesList(ForceUpdateDTO dto, Student student) {
        StringBuilder sb = new StringBuilder();

        addChange(sb, "Статус", student.getStatus() != null ? student.getStatus().getDisplayName() : null, dto.getStatus());
        addChange(sb, "Место практики", student.getPracticePlace() != null ? student.getPracticePlace().getDisplayName() : null, dto.getPracticePlace());
        addChange(sb, "Формат практики", student.getPracticeFormat() != null ? student.getPracticeFormat().getDisplayName() : null, dto.getPracticeFormat());
        addInnChange(sb, student.getCompanyINN(), dto.getCompanyINN());
        addChange(sb, "Компания", student.getCompanyName(), dto.getCompanyName());
        addChange(sb, "Руководитель", student.getCompanyLeadFullName(), dto.getCompanyLeadFullName());
        addChange(sb, "Телефон", student.getCompanyLeadPhone(), dto.getCompanyLeadPhone());
        addChange(sb, "Email", student.getCompanyLeadEmail(), dto.getCompanyLeadEmail());
        addChange(sb, "Должность", student.getCompanyLeadJobTitle(), dto.getCompanyLeadJobTitle());

        return sb.toString();
    }

    /**
     * Добавляет строку изменения в StringBuilder.
     *
     * @param sb      StringBuilder для накопления строк
     * @param field   имя поля
     * @param oldVal  старое значение
     * @param newVal  новое значение
     */
    private void addChange(StringBuilder sb, String field, String oldVal, String newVal) {
        if (newVal == null || newVal.isBlank()) return;

        String old = (oldVal == null || oldVal.isBlank()) ? "-" : oldVal;
        sb.append("  ").append(field).append(": ").append(old).append(" -> ").append(newVal).append("\n");
    }

    /**
     * Добавляет строку изменения для ИНН
     *
     * @param sb      StringBuilder для накопления строк
     * @param oldInn  старый ИНН
     * @param newInn  новый ИНН
     */
    private void addInnChange(StringBuilder sb, Long oldInn, String newInn) {
        if (newInn == null || newInn.isBlank()) return;

        String old = oldInn == null ? "-" : oldInn.toString();
        sb.append("  ИНН: ").append(old).append(" -> ").append(newInn).append("\n");
    }

    /**
     * Возвращает true, так как после этой команды требуется подтверждение.
     *
     * @return true
     */
    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    /**
     * Возвращает имя команды.
     *
     * @return имя команды "/forceupdate"
     */
    @Override
    public String getName() {
        return "/forceupdate";
    }

    /**
     * Возвращает описание команды для справки.
     *
     * @return текстовое описание команды
     */
    @Override
    public String getDescription() {
        return "Принудительно обновить данные студента. Ключи: --help, --dry-run. Пример: /forceupdate 123456 \"Поток\" status=\"PRACTICE_APPROVED\"";
    }
}
