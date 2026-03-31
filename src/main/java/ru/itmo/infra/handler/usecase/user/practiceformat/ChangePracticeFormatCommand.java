package ru.itmo.infra.handler.usecase.user.practiceformat;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.infra.handler.usecase.user.UserCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.company.AskingPracticeFormatCommand;

public class ChangePracticeFormatCommand implements UserCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        ContextHolder.setNextCommand(chatId, new ChangePracticeFormatHandleCommand());
        return MessageToUser.builder()
                .text("Выберите новый формат прохождения практики:")
                .keyboardMarkup(AskingPracticeFormatCommand.getPracticeFormatKeyboard())
                .needRewriting(true)
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/change_practice_format";
    }

    @Override
    public String getDescription() {
        return "Изменить формат прохождения практики";
    }

    @Override
    public String getDisplayName() {
        return "Изменить формат практики";
    }

    @Override
    public boolean isAvailableForStatus(StudentStatus status) {
        if (status == null || status == StudentStatus.NOT_REGISTERED) return false;
        return status != StudentStatus.APPLICATION_WAITING_SIGNING
                && status != StudentStatus.APPLICATION_SIGNED;
    }
}

