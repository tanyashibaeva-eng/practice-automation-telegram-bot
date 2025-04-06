package ru.itmo.bot;

import lombok.Getter;
import lombok.extern.java.Log;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.itmo.interceptor.Interceptor;
import ru.itmo.util.PropertiesProvider;

import java.io.File;

@Log
public class PracticeAutomationBot implements LongPollingMultiThreadUpdateConsumer {

    // TODO: see if there's any alternatives to the OkHttpTelegramClient and whether we should use different TelegramClient implementation
    @Getter
    private static final TelegramClient telegramClient = new OkHttpTelegramClient(PropertiesProvider.getToken());

    @Override
    public void consume(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();
            MessageToUser response = Interceptor.processMessage(message);
            if (response.getDocument() == null) {
                sendMessage(response.getText(), chatId);
            } else {
                sendDocument(response.getDocument(), response.getText(), chatId);
            }
        }
    }

    // TODO: подумать над тем как прокидывать сюда кнопки/несколько сообщений
    private static void sendMessage(String message, long chatId) {
        SendMessage sendMessage = SendMessage
                .builder()
                .chatId(chatId)
                .text(message)
                .build();

        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException ex) {
            log.severe("Не удалось отправить сообщение: " + ex.getMessage());
        }
    }

    private static void sendDocument(File file, String caption, long chatId) {
        SendDocument sendDocument = SendDocument.builder()
                .chatId(chatId)
                .document(new InputFile(file))
                .caption(caption)
                .build();
        try {
            telegramClient.execute(sendDocument);
        } catch (TelegramApiException ex) {
            log.severe("Не удалось отправить документ: " + ex.getMessage());
        }
    }
}