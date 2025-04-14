package ru.itmo.infra.handler.usecase.admin.uploadexcel;

import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

public class UploadExcelCommand implements AdminCommand {
    @Override
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        ContextHolder.setNextCommand(chatId, new UploadExcelHandleCommand());
        return MessageToUser.builder().text("Хорошо, давайте загрузим файл! Кидайте его!").build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/upload";
    }

    @Override
    public String getDescription() {
        return "Загрузить excel файл для обновления студентов в потоке. Пример: `/upload Бакалавры 2025`";
    }
}
