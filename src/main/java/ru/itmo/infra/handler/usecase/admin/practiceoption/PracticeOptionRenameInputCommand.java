package ru.itmo.infra.handler.usecase.admin.practiceoption;

import ru.itmo.application.ContextHolder;
import ru.itmo.application.PracticeOptionService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

public class PracticeOptionRenameInputCommand implements AdminCommand {
    @Override
    public MessageToUser execute(MessageDTO message) {
        var text = message.getText() == null ? "" : message.getText().trim();
        if ("Вернуться в меню".equalsIgnoreCase(text)) {
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder().text("Возврат в меню").build();
        }
        try {
            var id = (Long) ContextHolder.getCommandData(message.getChatId());
            PracticeOptionService.renameOption(id, text);
            ContextHolder.setNextCommand(message.getChatId(), new ListPracticeOptionsCommand());
            return MessageToUser.builder().text("Название обновлено").build();
        } catch (Exception e) {
            ContextHolder.setNextCommand(message.getChatId(), this);
            return MessageToUser.builder()
                    .text("Не удалось переименовать. Введите новое название еще раз.")
                    .keyboardMarkup(getReturnToStartMarkup())
                    .build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }
}
