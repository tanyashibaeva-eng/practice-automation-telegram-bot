package ru.itmo.infra.handler.usecase.admin.filledustream;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.exception.UnknownUserException;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

public class FillEduStreamMoreFilesCommand implements AdminCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            var streamName = getEduStreamNameOrThrow(message);
            String response = message.getText();

            switch (response) {
                case "Да":
                    ContextHolder.setNextCommand(message.getChatId(), new FillEduStreamUploadCommand());
                    return MessageToUser.builder()
                            .text("Пожалуйста, прикрепите следующий файл с группой для потока \"%s\"".formatted(streamName))
                            .keyboardMarkup(new ReplyKeyboardRemove(true))
                            .keyboardMarkup(getReturnToStartMarkup())
                            .needRewriting(true)
                            .build();

                case "Нет":
                    ContextHolder.endCommand(message.getChatId());
                    return MessageToUser.builder()
                            .text("Все файлы загружены. Возврат в главное меню")
                            .build();

                default:
                    ContextHolder.setNextCommand(message.getChatId(), this);
                    return MessageToUser.builder()
                            .text("Извините, я вас не понимаю. Ответьте \"Да\" или \"Нет\"")
                            .keyboardMarkup(getConfirmationKeyboard())
                            .build();
            }
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