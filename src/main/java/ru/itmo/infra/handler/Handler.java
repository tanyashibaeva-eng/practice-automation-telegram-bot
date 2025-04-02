package ru.itmo.infra.handler;

import lombok.extern.java.Log;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.itmo.bot.PracticeAutomationBot;

import java.io.IOException;
import java.nio.file.Files;

@Log
public class Handler {

    private static final TelegramClient telegramClient = PracticeAutomationBot.getTelegramClient();

    public static String handleMessage(Message message) throws TelegramApiException, IOException {
        if (message.hasText())
            return processTextMessage(message);

        else if (message.hasDocument())
            return processDocumentMessage(message);

        else
            return "Я не знаю, как на это ответить";
    }

    private static String processTextMessage(Message message) {
        String messageText = message.getText();

        /*
        do something else
        */

        return messageText; // just echo the message
    }

    private static String processDocumentMessage(Message message) throws TelegramApiException, IOException {
        GetFile getFile = buildGetFile(message);

        File file = telegramClient.execute(getFile);
        var downloaded = telegramClient.downloadFile(file);

        String text = Files.readString(downloaded.toPath()); // we can also read bytes with Files.readAllBytes()
        return ("Содержимое файла: " + text);
    }

    private static GetFile buildGetFile(Message message) {
        String fileId = message.getDocument().getFileId();
        String fileName = message.getDocument().getFileName();
        String fileMimeType = message.getDocument().getMimeType();
        long fileSize = message.getDocument().getFileSize();

        Document document = new Document();
        document.setMimeType(fileMimeType);
        document.setFileName(fileName);
        document.setFileSize(fileSize);
        document.setFileId(fileId);

        GetFile getFile = new GetFile(fileId);
        getFile.setFileId(document.getFileId());

        return getFile;
    }
}
