package ru.itmo.infra.handler.usecase.user.studentapplicationinput;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.Handler;
import ru.itmo.infra.handler.usecase.Command;

public class ApplicationInfoSubmittedCommand implements Command {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        // TODO:
        StudentService.updateApplicationBytesByChatIdAndEduStreamName(
                message.getChatId(),
                ContextHolder.getEduStreamName(message.getChatId()),
                Handler.getFileFromMessage(message));
        ContextHolder.endCommand(message.getChatId());
        return MessageToUser.builder()
                .text("Спасибо за информацию! Заявка отправлена на проверку преподавателю")
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }
}
