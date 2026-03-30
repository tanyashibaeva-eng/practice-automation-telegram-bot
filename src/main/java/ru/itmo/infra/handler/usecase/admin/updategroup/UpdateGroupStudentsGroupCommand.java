package ru.itmo.infra.handler.usecase.admin.updategroup;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

public class UpdateGroupStudentsGroupCommand implements AdminCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        long chatId = message.getChatId();
        String groupNumber = message.getText().trim();

        if (groupNumber.isEmpty()) {
            ContextHolder.setNextCommand(chatId, this);
            return MessageToUser.builder()
                    .text("Номер группы не может быть пустым. Попробуйте еще раз:")
                    .keyboardMarkup(null)
                    .needRewriting(true)
                    .build();
        }

        ContextHolder.setCommandData(chatId, groupNumber);

        ContextHolder.setNextCommand(chatId, new UpdateGroupStudentsFileCommand());

        return MessageToUser.builder()
                .text("Загрузите Excel-файл со списком студентов группы " + groupNumber)
                .keyboardMarkup(getReturnToStartMarkup())
                .needRewriting(true)
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }
}