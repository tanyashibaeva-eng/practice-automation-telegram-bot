package ru.itmo.infra.handler;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageToUser;

public class ExportStudentsExcelFile {

    @SneakyThrows
    public static MessageToUser start(Message message) {
        var chatId = message.getChatId();
        var eduStreamName = "stream 1";
        var file = StudentService.exportStudentsToExcel(eduStreamName);
        Handler.endCommand(chatId);
        return  MessageToUser.builder().text("Хорошо вот он!").document(file).build();
    }
}
