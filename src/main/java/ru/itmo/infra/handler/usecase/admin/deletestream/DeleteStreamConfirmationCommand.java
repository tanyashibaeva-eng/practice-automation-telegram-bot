package ru.itmo.infra.handler.usecase.admin.deletestream;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.EduStreamService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.model.EduStream;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.UnknownUserException;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;
import ru.itmo.infra.handler.usecase.admin.gotostream.GotoStreamCommand;

public class DeleteStreamConfirmationCommand implements AdminCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            String response = message.getText();
            var streamName = getEduStreamNameOrThrow(message);

            switch (response) {
                case "Да":
                    EduStream stream = new EduStream(streamName);
                    EduStreamService.deleteEduStream(stream);
                    ContextHolder.endCommand(message.getChatId());
                    return MessageToUser.builder()
                            .text("Поток '%s' успешно удален".formatted(streamName))
                            .keyboardMarkup(new ReplyKeyboardRemove(true))
                            .build();
                case "Нет":
                    ContextHolder.setNextCommand(message.getChatId(), new GotoStreamCommand());
                    return MessageToUser.builder()
                            .text("Удаление потока отменено")
                            .keyboardMarkup(new ReplyKeyboardRemove(true))
                            .build();
                default:
                    ContextHolder.setNextCommand(message.getChatId(), this);
                    return MessageToUser.builder()
                            .text("Пожалуйста, ответьте 'Да' или 'Нет'")
                            .build();
            }
        } catch (BadRequestException e) {
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text("Ошибка: " + e.getMessage())
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .build();
        } catch (UnknownUserException e) {
            return returnToMainMenuWithError(message.getChatId(),
                    "Из-за внешних обстоятельств контекст был утерян. Пожалуйста, повторите действие еще раз");
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }
}