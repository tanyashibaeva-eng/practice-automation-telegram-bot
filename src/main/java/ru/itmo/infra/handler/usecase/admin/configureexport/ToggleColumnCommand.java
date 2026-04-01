package ru.itmo.infra.handler.usecase.admin.configureexport;

import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

import java.util.Set;

public class ToggleColumnCommand implements AdminCommand {

    @Override
    public MessageToUser execute(MessageDTO message) {
        Long chatId = message.getChatId();

        String colName = ContextHolder.getCurrentColumn(chatId);
        StudentColumn column = StudentColumn.valueOf(colName);

        Set<StudentColumn> selected = ContextHolder.getSelectedColumns(chatId);

        if (selected.contains(column)) {
            selected.remove(column);
        } else {
            selected.add(column);
        }

        return MessageToUser.builder()
                .text("Выберите столбцы (" + selected.size() + "/17):")
                .keyboardMarkup(ConfigureExportCommand.buildKeyboard(chatId))
                .needRewriting(true)
                .build();
    }

    @Override
    public String getName() {
        return "/toggle_column";
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }
}