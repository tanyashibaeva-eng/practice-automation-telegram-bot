package ru.itmo.infra.handler;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.itmo.application.StudentService;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.exception.InvalidMessageException;

public class UploadStudentsExcelFile {

    @SneakyThrows
    public static MessageToUser start(MessageDTO message) {
        var chatId = message.getChatId();
        Handler.setNextCommandFunction(chatId, UploadStudentsExcelFile::upload);
        return MessageToUser.builder().text("Хорошо давайте загрузим файл! Кидайте его!").keyboardMarkup(getMarkupKeyboardForStart()).build();
    }

    @SneakyThrows
    public static MessageToUser upload(MessageDTO message) {
        var chatId = message.getChatId();
        var file = Handler.getFileFromMessage(message);
//        var eduStreamId = Handler.getStreamEduId(chatId);

        var res = StudentService.updateStudentsFromExcel(file, 1);
        if (res.isEmpty()) {
            Handler.setNextCommandFunction(chatId, UploadStudentsExcelFile::feedback);
            return MessageToUser.builder().text("Файл был загружен, вам понравилось?").build();
        }
        return MessageToUser.builder()
                .text("В загруженном файле содержаться ошибки, поправьте их и попробуйте снова или вернитесь назад.")
                .document(res.get())
                .build();
    }

    @SneakyThrows
    public static MessageToUser feedback(MessageDTO message) {
        var chatId = message.getChatId();
        var command = Handler.getTextFromMessage(message);

        if (command.equals("Да")) {
            Handler.endCommand(chatId);
            return MessageToUser.builder().text("Очень рад!").build();
        }
        if (command.equals("Нет")) {
            Handler.endCommand(chatId);
            return MessageToUser.builder().text("Простите, я буду стараться лучше!(").build();
        }

        throw new InvalidMessageException("Я вас не понимаю, введите \"Да\" или \"Нет\"");
    }

    private static ReplyKeyboard getMarkupKeyboardForStart() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(
                        new InlineKeyboardRow(
                                InlineKeyboardButton.builder()
                                        .text("поток 1")
                                        .callbackData(
                                                CallbackData.builder()
                                                        .command("/showEduStreamInfo")
                                                        .key("eduStreamName")
                                                        .value("поток 1")
                                                        .build()
                                                        .toString()
                                        ).build()
                        )).build();
    }
}
