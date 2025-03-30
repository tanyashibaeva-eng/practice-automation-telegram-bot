package ru.itmo.bot;

import lombok.extern.java.Log;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.itmo.util.PropertiesProvider;

import java.io.IOException;
import java.nio.file.Files;

@Log
public class PracticeAutomationBot implements LongPollingMultiThreadUpdateConsumer {

    // TODO: see if there's any alternatives to the OkHttpTelegramClient and whether we should use different TelegramClient implementation
    private final TelegramClient telegramClient = new OkHttpTelegramClient(PropertiesProvider.getToken());

    @Override
    public void consume(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();

            if (message.hasText())
                processTextMessage(message);

            else if (message.hasDocument())
                processDocumentMessage(message);

            else
                sendMessage("I don't know how to answer this yet", message.getChatId());
        }
    }

    private void processTextMessage(Message message) {
        String messageText = message.getText();
        long chatId = message.getChatId();

        /*
        do something else
        */

        sendMessage(messageText, chatId); // just echo the message
    }

    private void processDocumentMessage(Message message) {
        GetFile getFile = buildGetFile(message);

        try {
            File file = telegramClient.execute(getFile);
            var downloaded = telegramClient.downloadFile(file);

            String text = Files.readString(downloaded.toPath()); // we can also read bytes with Files.readAllBytes()
            sendMessage("Contents of the sent file: " + text, message.getChatId());

        } catch (TelegramApiException ex) {
            log.severe("Could not download file: " + ex.getMessage());
        } catch (IOException ex) {
            log.severe("Could not read string from downloaded file: " + ex.getMessage());
        }
    }

    private GetFile buildGetFile(Message message) {
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

    private void sendMessage(String message, long chatId) {
        SendMessage sendMessage = SendMessage
                .builder()
                .chatId(chatId)
                .text(message)
                .build();

        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException ex) {
            log.severe("Could not send message: " + ex.getMessage());
        }
    }
}