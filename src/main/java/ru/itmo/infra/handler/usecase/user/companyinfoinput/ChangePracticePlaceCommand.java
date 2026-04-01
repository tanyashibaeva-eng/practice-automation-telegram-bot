package ru.itmo.infra.handler.usecase.user.companyinfoinput;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.infra.handler.usecase.user.UserCommand;

import static ru.itmo.infra.handler.usecase.user.companyinfoinput.ChoosePracticePlaceCommand.getPracticePlaceKeyboard;

public class ChangePracticePlaceCommand implements UserCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        ContextHolder.setNextCommand(chatId, new PracticeConfirmationCommand());
        return MessageToUser.builder()
                .text("Выберите место прохождения практики:")
                .keyboardMarkup(getPracticePlaceKeyboard())
                .build();
    }

    @Override
    public String getDisplayName() {
        return "Поменять данные о практике";
    }

    @Override
    public boolean isAvailableForStatus(StudentStatus status) {
        return status != StudentStatus.NOT_REGISTERED && status != StudentStatus.REGISTERED;
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/change_practice_place";
    }

    @Override
    public String getDescription() {
        return "Поменять ранее указанные данные о месте практики";
    }
}