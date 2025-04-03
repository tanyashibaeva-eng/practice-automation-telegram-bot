package ru.itmo.infra.handler;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.itmo.application.StudentService;
import ru.itmo.exception.InvalidMessageException;

public class UploadStudentsExcelFile {

    @SneakyThrows
    public static String start(Message message) {
        var chatID = message.getChatId();
        Handler.setNextCommandFunction(chatID, UploadStudentsExcelFile::upload);
        return "Хорошо давайте загрузим файл! Кидайте его!";
    }

    @SneakyThrows
    public static String upload(Message message) {
        var chatID = message.getChatId();
        var file = Handler.getFileFromMessage(message);

        StudentService studentService = new StudentService();
        var res = studentService.updateStudentsFromExcel(file);
        System.out.println(res);

        Handler.setNextCommandFunction(chatID, UploadStudentsExcelFile::feedback);
        return "Файл был загружен, вам понравилось?";
    }

    @SneakyThrows
    public static String feedback(Message message) {
        var chatID = message.getChatId();
        var command = Handler.getTextFromMessage(message);

        if (command.equals("Да")) {
            Handler.endCommand(chatID);
            return "Очень рад!";
        }
        if (command.equals("Нет")) {
            Handler.endCommand(chatID);
            return "Простите, я буду стараться лучше!(";
        }

        throw new InvalidMessageException("Я вас не понимаю, введите \"Да\" или \"Нет\"");
    }
}
