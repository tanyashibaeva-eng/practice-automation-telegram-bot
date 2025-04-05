package ru.itmo.infra.handler;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.itmo.application.StudentService;
import ru.itmo.exception.InvalidMessageException;

public class UploadStudentsExcelFile {

    @SneakyThrows
    public static String start(Message message) {
        var chatId = message.getChatId();
        Handler.setNextCommandFunction(chatId, UploadStudentsExcelFile::upload);
        return "Хорошо давайте загрузим файл! Кидайте его!";
    }

    @SneakyThrows
    public static String upload(Message message) {
        var chatId= message.getChatId();
        var file = Handler.getFileFromMessage(message);
        var eduStreamId = Handler.getStreamEduId(chatId);

        StudentService studentService = new StudentService();
        var res = studentService.updateStudentsFromExcel(file, eduStreamId);
        System.out.println(res);

        Handler.setNextCommandFunction(chatId, UploadStudentsExcelFile::feedback);
        return "Файл был загружен, вам понравилось?";
    }

    @SneakyThrows
    public static String feedback(Message message) {
        var chatId = message.getChatId();
        var command = Handler.getTextFromMessage(message);

        if (command.equals("Да")) {
            Handler.endCommand(chatId);
            return "Очень рад!";
        }
        if (command.equals("Нет")) {
            Handler.endCommand(chatId);
            return "Простите, я буду стараться лучше!(";
        }

        throw new InvalidMessageException("Я вас не понимаю, введите \"Да\" или \"Нет\"");
    }
}
