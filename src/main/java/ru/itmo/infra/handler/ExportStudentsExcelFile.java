package ru.itmo.infra.handler;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageToUser;
import ru.itmo.exception.InvalidMessageException;

public class ExportStudentsExcelFile {

    @SneakyThrows
    public static MessageToUser start(Message message) {
        var chatId = message.getChatId();
        var eduId = 1;
        var file = StudentService.exportStudentsToExcel(eduId);
        Handler.endCommand(chatId);
        return  MessageToUser.builder().text("Хорошо вот он!").document(file).build();
    }
}
