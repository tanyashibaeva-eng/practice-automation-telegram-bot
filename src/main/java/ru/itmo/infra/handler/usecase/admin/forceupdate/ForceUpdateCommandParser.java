package ru.itmo.infra.handler.usecase.admin.forceupdate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.itmo.domain.dto.ForceUpdateDTO;
import ru.itmo.exception.BadRequestException;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Парсер для команды /forceupdate с поддержкой синтаксиса field=value.
 * <p>
 * Обрабатывает следующие форматы команды:
 * <ul>
 *     <li>{@code /forceupdate --help} - показать доступные поля</li>
 *     <li>{@code /forceupdate --dry-run <chatId> "<поток>" поле="значение"...} - режим предпросмотра</li>
 *     <li>{@code /forceupdate <chatId> "<поток>" поле="значение"...} - выполнить обновление</li>
 * </ul>
 * <p>
 * Примеры использования:
 * <ul>
 *     <li>{@code /forceupdate 123456789 "Тестовый поток 2026" status="PRACTICE_APPROVED"}</li>
 *     <li>{@code /forceupdate 123456789 "Весна 2026" place="ITMO_COMPANY" format="OFFLINE"}</li>
 *     <li>{@code /forceupdate --dry-run 123456789 "Поток" status="PRACTICE_APPROVED"}</li>
 * </ul>
 *
 */
@Getter
@RequiredArgsConstructor
public class ForceUpdateCommandParser {

    /**
     * Паттерн для ключа --help.
     */
    private static final Pattern HELP_PATTERN = Pattern.compile(
            "^/forceupdate\\s+--help$"
    );

    /**
     * Базовый паттерн команды.
     * Извлекает: /forceupdate, --dry-run (опционально), chatId, имя потока в кавычках, параметры.
     */
    private static final Pattern BASE_PATTERN = Pattern.compile(
            "^/forceupdate(\\s+--dry-run)?\\s+(\\d+)\\s+\"([^\"]+)\"\\s*(.*)$"
    );

    /**
     * Паттерн для параметров field="value".
     * Работает с любым количеством пробелов между параметрами.
     */
    private static final Pattern PARAM_PATTERN = Pattern.compile(
            "([a-zA-Zа-яА-ЯёЁ_]+)=\"([^\"]*)\""
    );

    /**
     * Флаг режима предпросмотра (dry-run).
     */
    private final boolean dryRun;

    /**
     * Флаг запроса справки (--help).
     */
    private final boolean showFields;

    /**
     * ChatId студента.
     */
    private final long studentChatId;

    /**
     * Имя потока (eduStreamName).
     */
    private final String eduStreamName;

    /**
     * Карта распарсенных полей и их значений.
     */
    private final Map<ForceUpdateField, String> fieldValues;

    /**
     * Парсит текст сообщения команды.
     *
     * @param messageText текст сообщения от пользователя
     * @return распарсенный объект ForceUpdateCommandParser
     * @throws BadRequestException если формат команды неверный
     */
    public static ForceUpdateCommandParser parse(String messageText) throws BadRequestException {
        String trimmedMessage = messageText.trim();

        if (trimmedMessage.equals("/forceupdate")) {
            throw new BadRequestException(buildUsageMessage());
        }

        Matcher helpMatcher = HELP_PATTERN.matcher(trimmedMessage);
        if (helpMatcher.matches()) {
            return new ForceUpdateCommandParser(false, true, 0, null, Map.of());
        }

        Matcher matcher = BASE_PATTERN.matcher(trimmedMessage);

        if (!matcher.matches()) {
            throw new BadRequestException(buildUsageMessage());
        }

        boolean dryRun = matcher.group(1) != null;

        long chatId;
        try {
            chatId = Long.parseLong(matcher.group(2));
        } catch (NumberFormatException e) {
            throw new BadRequestException("Неверный тип chatId, ожидалось число");
        }

        String eduStreamName = matcher.group(3).trim();
        String paramsPart = matcher.group(4);

        Map<ForceUpdateField, String> parsedFields = parseParams(paramsPart);

        return new ForceUpdateCommandParser(dryRun, false, chatId, eduStreamName, parsedFields);
    }

    /**
     * Парсит параметры field="value" из строки.
     *
     * @param paramsPart часть сообщения с параметрами
     * @return карта полей и их значений
     * @throws BadRequestException если найдено неизвестное поле или ошибка валидации
     */
    private static Map<ForceUpdateField, String> parseParams(String paramsPart) throws BadRequestException {
        Map<ForceUpdateField, String> parsedFields = new HashMap<>();

        if (paramsPart == null || paramsPart.isBlank()) {
            return parsedFields;
        }

        Matcher paramMatcher = PARAM_PATTERN.matcher(paramsPart);
        while (paramMatcher.find()) {
            String key = paramMatcher.group(1);
            String value = paramMatcher.group(2);

            ForceUpdateField field = ForceUpdateField.findByKey(key);
            if (field == null) {
                throw new BadRequestException(
                        "Неизвестное поле: \"" + key + "\"\n\n" +
                        "Допустимые поля:\n" + ForceUpdateField.getAvailableFieldsList()
                );
            }

            if (parsedFields.containsKey(field)) {
                throw new BadRequestException("Поле \"" + key + "\" указано несколько раз");
            }

            try {
                field.getValidator().validate(value);
            } catch (BadRequestException e) {
                throw new BadRequestException("Ошибка валидации поля \"" + key + "\":\n" + e.getMessage());
            }

            parsedFields.put(field, value.trim());
        }

        return parsedFields;
    }

    /**
     * Проверяет, указано ли хотя бы одно поле для обновления.
     *
     * @return true если есть поля для обновления
     */
    public boolean hasFieldsToUpdate() {
        return !fieldValues.isEmpty();
    }

    /**
     * Проверяет, запущен ли режим предпросмотра (dry-run).
     *
     * @return true если включен режим dry-run
     */
    public boolean isDryRun() {
        return dryRun;
    }

    /**
     * Конвертирует распарсенные данные в DTO для передачи в сервис.
     *
     * @return объект ForceUpdateDTO с данными для обновления
     */
    public ForceUpdateDTO toDTO() {
        var builder = ForceUpdateDTO.builder()
                .chatId(studentChatId)
                .eduStreamName(eduStreamName);

        for (Map.Entry<ForceUpdateField, String> entry : fieldValues.entrySet()) {
            switch (entry.getKey()) {
                case STATUS -> builder.status(entry.getValue());
                case PRACTICE_PLACE -> builder.practicePlace(entry.getValue());
                case PRACTICE_FORMAT -> builder.practiceFormat(entry.getValue());
                case COMPANY_INN -> builder.companyINN(entry.getValue());
                case COMPANY_NAME -> builder.companyName(entry.getValue());
                case LEAD_FULL_NAME -> builder.companyLeadFullName(entry.getValue());
                case LEAD_PHONE -> builder.companyLeadPhone(entry.getValue());
                case LEAD_EMAIL -> builder.companyLeadEmail(entry.getValue());
                case LEAD_JOB_TITLE -> builder.companyLeadJobTitle(entry.getValue());
            }
        }

        return builder.build();
    }

    /**
     * Формирует сообщение с инструкцией по использованию команды.
     *
     * @return строка с инструкцией
     */
    private static String buildUsageMessage() {
        return """
                Неверный формат команды /forceupdate.

                Ключи:
                /forceupdate --help - показать доступные поля
                /forceupdate --dry-run <chatId> "<поток>" поле="значение"... - режим предпросмотра

                Синтаксис:
                /forceupdate <chatId> "<поток>" поле="значение"...

                Примеры:
                /forceupdate 123456789 "Весна 2026" status="PRACTICE_APPROVED"

                /forceupdate 123456789 "Весна 2026" status="PRACTICE_APPROVED" place="ITMO_COMPANY" format="OFFLINE" company="ООО Ромашка" inn="7801234567" lead="Иванов И.И." phone="+79000000000" email="boss@company.com" title="Директор"
                """;
    }
}
