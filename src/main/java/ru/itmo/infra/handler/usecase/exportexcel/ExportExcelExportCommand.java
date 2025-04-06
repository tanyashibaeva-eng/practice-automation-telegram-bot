package ru.itmo.infra.handler.usecase.exportexcel;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.Command;

public class ExportExcelExportCommand implements Command {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var eduId = 1;
        var file = StudentService.exportStudentsToExcel(eduId);
        ContextHolder.endCommand(chatId);
        var text = message.getText();
        return MessageToUser.builder().text("Сгенерированная выгрузка:").document(file).build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "/export";
    }
}
