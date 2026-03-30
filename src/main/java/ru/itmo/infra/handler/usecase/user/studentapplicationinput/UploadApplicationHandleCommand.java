package ru.itmo.infra.handler.usecase.user.studentapplicationinput;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.Handler;
import ru.itmo.infra.handler.usecase.Command;

public class UploadApplicationHandleCommand implements Command {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        if (!message.hasDocument()) {
            return MessageToUser.builder()
                    .text("Пожалуйста, загрузите файл заявки")
                    .keyboardMarkup(getReturnToStartMarkup())
                    .needRewriting(true)
                    .build();
        }

        try {
            var file = Handler.getFileFromMessage(message);
            StudentService.updateApplicationBytesByChatIdAndEduStreamName(
                    message.getChatId(),
                    ContextHolder.getEduStreamName(message.getChatId()),
                    file);

            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text("Заявка успешно загружена и отправлена на проверку")
                    .build();
        } catch (Exception e) {
            return MessageToUser.builder()
                    .text("Ошибка при загрузке заявки: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }
}