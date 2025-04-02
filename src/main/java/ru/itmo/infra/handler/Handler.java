package ru.itmo.infra.handler;

import lombok.extern.java.Log;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.itmo.application.TelegramUserService;
import ru.itmo.bot.PracticeAutomationBot;
import ru.itmo.exception.InvalidMessageException;
import ru.itmo.exception.UnknownUserException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Function;

import static ru.itmo.exception.InvalidMessageException.ThrowDocumentException;
import static ru.itmo.exception.InvalidMessageException.ThrowMessageException;

@Log
public class Handler {

    private static final TelegramClient telegramClient = PracticeAutomationBot.getTelegramClient();
    private static final TelegramUserService telegramUserService = new TelegramUserService();
    private static final HashMap<String, Function<Message, String>> commands = new HashMap<>();

    static {
        commands.put("/start", GreetingCommand::greetingAdminCommand);
        commands.put(null, GreetingCommand::greetingAdminCommand);
        commands.put("/upload", UploadStudentsExcelFile::start);

    }

    public static String handleMessage(Message message) throws Exception {
        Function<Message, String> nextFunc;
        try {
            nextFunc = telegramUserService.getNextFunction(message.getChatId());
        } catch (UnknownUserException e) {
            nextFunc = null;
        }


        if (nextFunc != null) {
            return nextFunc.apply(message);
        }

        var command = message.getText();
        if (!commands.containsKey(command)) {
            return "Извините, но я не понимаю такую команду. Попробуйте другую или напишите \"/help\" для помощи";
        }

        return commands.get(command).apply(message);
    }

    public static String getTextFromMessage(Message message) throws InvalidMessageException {
        if (!message.hasText()) {
            ThrowMessageException();
        }
        return message.getText();
    }

    public static File getFileFromMessage(Message message) throws TelegramApiException, IOException, InvalidMessageException {
        if (!message.hasDocument()) {
            ThrowDocumentException();
        }

        var fileId = message.getDocument().getFileId();
        var fileName = message.getDocument().getFileName();
        var fileMimeType = message.getDocument().getMimeType();
        var fileSize = message.getDocument().getFileSize();

        var document = new Document();
        document.setMimeType(fileMimeType);
        document.setFileName(fileName);
        document.setFileSize(fileSize);
        document.setFileId(fileId);

        var getFile = new GetFile(fileId);
        getFile.setFileId(document.getFileId());

        var tgFile = telegramClient.execute(getFile);
        return telegramClient.downloadFile(tgFile).toPath().toFile();
    }

    public static void setNextCommandFunction(Long chatId, Function<Message, String> handler) {
        telegramUserService.setNextFunction(chatId, handler);
    }

    public static void endCommand(Long chatId) {
        telegramUserService.removeChatID(chatId);
    }
}
