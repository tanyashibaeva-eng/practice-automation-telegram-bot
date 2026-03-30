package ru.itmo.infra.handler.usecase.user.studentapplicationinput;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.infra.handler.usecase.user.UserCommand;

public class UploadApplicationCommand implements UserCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        ContextHolder.setNextCommand(message.getChatId(), new UploadApplicationHandleCommand());
        return MessageToUser.builder()
                .text("Загрузите заявку")
                .keyboardMarkup(getReturnToStartMarkup())
                .needRewriting(true)
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/upload_application";
    }

    @Override
    public String getDescription() {
        return "Загрузить заполненную заявку";
    }

    @Override
    public String getDisplayName() {
        return "Загрузить заполненную заявку";
    }

    @Override
    public boolean isAvailableForStatus(StudentStatus status) {
        return status == StudentStatus.APPLICATION_WAITING_SUBMISSION
                || status == StudentStatus.APPLICATION_RETURNED;
    }
}
