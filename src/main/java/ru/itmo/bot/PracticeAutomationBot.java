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

@Log
public class PracticeAutomationBot implements LongPollingMultiThreadUpdateConsumer {

    // TODO: see if there's any alternatives to the OkHttpTelegramClient and whether we should use different TelegramClient implementation
    @Getter
    private static final TelegramClient telegramClient = new OkHttpTelegramClient(PropertiesProvider.getToken());

    @Override
    public void consume(Update update) {
        MessageToUser response = null;
        long chatId = 0;

        if (update.hasCallbackQuery()) {
            String callbackDataString = update.getCallbackQuery().getData();
            chatId = update.getCallbackQuery().getMessage().getChatId();
            MessageDTO messageDTO = MessageDTO.builder().chatId(chatId).build();
            response = Interceptor.processCallback(messageDTO, callbackDataString);
        }
        if (update.hasMessage()) {
            Message message = update.getMessage();
            chatId = message.getChatId();
            MessageDTO messageDTO = MessageDTO.builder().chatId(chatId).text(message.getText()).document(message.getDocument()).build();
            response = Interceptor.processMessage(messageDTO);
        }

        if (response == null) {
            return;
        }

        if (response.getDocument() == null) {
            sendMessage(response, chatId);
        } else {
            sendDocument(response, chatId);
        }
    }

    // TODO: подумать над тем как прокидывать сюда кнопки/несколько сообщений
    private static void sendMessage(MessageToUser message, long chatId) {
        SendMessage sendMessage = SendMessage
                .builder()
                .chatId(chatId)
                .text(message.getText())
                .replyMarkup(message.getKeyboardMarkup())
                .build();

        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException ex) {
            log.severe("Не удалось отправить сообщение: " + ex.getMessage());
        }
    }

    private static void sendDocument(MessageToUser message, long chatId) {
        SendDocument sendDocument = SendDocument.builder()
                .chatId(chatId)
                .document(new InputFile(message.getDocument()))
                .caption(message.getText())
                .replyMarkup(message.getKeyboardMarkup())
                .build();
        try {
            telegramClient.execute(sendDocument);
        } catch (TelegramApiException ex) {
            log.severe("Не удалось отправить документ: " + ex.getMessage());
        }
    }
}