package ru.itmo.infra.handler.usecase.uploadexcel;

import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.Command;

public class UploadExcelStartCommand implements Command {
    @Override
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        ContextHolder.setNextCommand(chatId, new UploadExcelUploadCommand());
        return MessageToUser.builder().text("Хорошо давайте загрузим файл! Кидайте его!").build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "";
    }
}
