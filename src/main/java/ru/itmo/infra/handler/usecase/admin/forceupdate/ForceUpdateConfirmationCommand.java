package ru.itmo.infra.handler.usecase.admin.forceupdate;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.ForceUpdateDTO;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

/**
 * Команда подтверждения принудительного обновления данных студента.
 * <p>
 * Является вторым этапом выполнения команды {@link ForceUpdateCommand}.
 * После того как администратор вводит команду /forceupdate с параметрами,
 * система запрашивает подтверждение: "Да" или "Нет".
 * <p>
 * Обрабатывает три варианта ответа:
 * <ul>
 *     <li>"Да" - применяет изменения к студенту через {@link StudentService#forceUpdateStudent}</li>
 *     <li>"Нет" - отменяет операцию и возвращает в главное меню</li>
 * </ul>
 * <p>
 * При успешном обновлении студент помечается как "managed_manually" = true,
 * что указывает на ручное управление данными в обход автоматических валидаций.
 *
 * @see ForceUpdateCommand
 * @see ForceUpdateDTO
 */
public class ForceUpdateConfirmationCommand implements AdminCommand {

    /**
     * Обрабатывает ответ пользователя на запрос подтверждения.
     * <p>
     * Процесс:
     * <ol>
     *     <li>Получает сохраненные данные команды из {@link ContextHolder}</li>
     *     <li>Проверяет ответ пользователя</li>
     *     <li>При подтверждении "Да" - вызывает {@link StudentService#forceUpdateStudent}</li>
     *     <li>При отказе "Нет" - очищает контекст и возвращает в меню</li>
     *     <li>При неверном ответе - повторно запрашивает подтверждение</li>
     * </ol>
     *
     * @param message входящее сообщение от пользователя
     * @return MessageToUser с результатом обработки
     */
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var args = (ForceUpdateDTO) ContextHolder.getCommandData(chatId);

        switch (message.getText()) {
            case "Да":
                var errors = StudentService.forceUpdateStudent(args);
                ContextHolder.endCommand(chatId);
                if (!errors.isEmpty()) {
                    return MessageToUser.builder()
                            .text("Ошибка во время ручного обновления студента: " + String.join(", ", errors))
                            .keyboardMarkup(new ReplyKeyboardRemove(true))
                            .build();
                }
                return MessageToUser.builder()
                        .text("Пользователь с chatId %d был изменен вручную".formatted(args.getChatId()))
                        .keyboardMarkup(new ReplyKeyboardRemove(true))
                        .needRewriting(false)
                        .build();
            case "Нет":
                ContextHolder.endCommand(chatId);
                return MessageToUser.builder()
                        .text("Возврат в главное меню")
                        .keyboardMarkup(new ReplyKeyboardRemove(true))
                        .build();
            default:
                ContextHolder.setNextCommand(chatId, this);
                return MessageToUser.builder()
                        .text("Ответьте \"Да\" или \"Нет\"")
                        .keyboardMarkup(getConfirmationKeyboard())
                        .build();
        }
    }

    /**
     * Возвращает true, так как команда ожидает ответ пользователя.
     *
     * @return true
     */
    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    /**
     * Возвращает пустую строку, так как это внутренняя команда подтверждения.
     *
     * @return пустая строка
     */
    @Override
    public String getName() {
        return "";
    }
}
