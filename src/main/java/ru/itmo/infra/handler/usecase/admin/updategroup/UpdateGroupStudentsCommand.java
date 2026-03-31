package ru.itmo.infra.handler.usecase.admin.updategroup;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

public class UpdateGroupStudentsCommand implements AdminCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        long chatId = message.getChatId();

        ContextHolder.setNextCommand(chatId, new UpdateGroupStudentsGroupCommand());

        return MessageToUser.builder()
                .text("Введите номер группы (например: P3430)")
                .keyboardMarkup(null)
                .needRewriting(true)
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/update_group_students";
    }
}