package ru.itmo.infra.handler;

import lombok.extern.java.Log;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
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
    private static final ContextHolder contextHolder = new ContextHolder();
    private static final HashMap<String, Function<MessageDTO, MessageToUser>> commands = new HashMap<>();

    static {
        commands.put("/start", GreetingCommand::greetingAdminCommand);
        commands.put(null, GreetingCommand::greetingAdminCommand);
        commands.put("/upload", UploadStudentsExcelFile::start);
        commands.put("/export", ExportStudentsExcelFile::start);
        commands.put("/showEduStreamInfo", ShowEduStreamInfo::start);
//        commands.put("/registration", StudentRegistration::startRegistration);

    }

    public static MessageToUser handleMessage(MessageDTO message) throws Exception {
        Function<MessageDTO, MessageToUser> nextFunc;
        try {
            nextFunc = contextHolder.getNextFunction(message.getChatId());
        } catch (UnknownUserException e) {
            nextFunc = null;
        }


        if (nextFunc != null) {
            return nextFunc.apply(message);
        }

        var command = message.getText();
        if (!commands.containsKey(command)) {
            return MessageToUser.builder().text("Извините, но я не понимаю такую команду. Попробуйте другую или напишите \"/help\" для помощи").build();
        }

        return commands.get(command).apply(message);
    }

    public static MessageToUser handleCallback(MessageDTO message, String callbackDataString) throws Exception {
        var callbackData = new CallbackData(callbackDataString);
        if (callbackData.getKey() != null) {
            mapKeyToFunc(message.getChatId(), callbackData.getKey(), callbackData.getValue());
        }
        return commands.get(callbackData.getCommand()).apply(message);
    }

    public static String getTextFromMessage(MessageDTO message) throws InvalidMessageException {
        if (!message.hasText()) {
            ThrowMessageException();
        }
        return message.getText();
    }

    public static File getFileFromMessage(MessageDTO message) throws TelegramApiException, IOException, InvalidMessageException {
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

    public static void setNextCommandFunction(Long chatId, Function<MessageDTO, MessageToUser> handler) {
        contextHolder.setNextFunction(chatId, handler);
    }

    public static void endCommand(Long chatId) {
        contextHolder.removeChatId(chatId);
    }

    public static void mapKeyToFunc(Long chatId, String key, String value) throws UnknownUserException {
        switch (key) {
            case "eduStreamName":
                setEduStreamName(chatId, value);
            default:
        }
    }

    public static String getEduStreamName(Long chatId) throws UnknownUserException {
        return contextHolder.getEduStreamName(chatId);
    }

    public static void setEduStreamName(Long chatId, String streamId) throws UnknownUserException {
        contextHolder.setEduStreamName(chatId, streamId);
    }
}
