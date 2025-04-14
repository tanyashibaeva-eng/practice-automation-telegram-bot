package ru.itmo.bot;

import lombok.Getter;
import lombok.extern.java.Log;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.itmo.application.ContextHolder;
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
        String username = update.getChatMember().getChat().getUserName();
        boolean isCallback = false;

        if (update.hasCallbackQuery()) {
            String callbackDataString = update.getCallbackQuery().getData();
            chatId = update.getCallbackQuery().getMessage().getChatId();
            MessageDTO messageDTO = MessageDTO.builder()
                    .chatId(chatId)
                    .username(username)
                    .text(callbackDataString)
                    .build();
            response = Interceptor.processCallback(messageDTO, callbackDataString);

            isCallback = !(response.getKeyboardMarkup() instanceof ReplyKeyboardMarkup || response.getDocument() != null);
        }
        if (update.hasMessage()) {
            Message message = update.getMessage();
            chatId = message.getChatId();
            MessageDTO messageDTO = MessageDTO.builder()
                    .chatId(chatId)
                    .username(username)
                    .text(message.getText())
                    .document(message.getDocument())
                    .build();
            response = Interceptor.processMessage(messageDTO);
        }

        sendToUser(response, chatId, isCallback);
    }

    public static void sendToUser(MessageToUser response, long chatId, boolean isCallback) {
        if (response == null) {
            return;
        }

        if (isCallback) {
            var lastMessageId = ContextHolder.getLastMessageId(chatId);
            editMessage(response, lastMessageId, chatId);
            return;
        }

        if (response.getDocument() == null) {
            sendMessage(response, chatId);
        } else {
            sendDocument(response, chatId);
        }
    }

    private static void sendMessage(MessageToUser message, long chatId) {
        var sendMessage = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(message.getText())
                .replyMarkup(message.getKeyboardMarkup())
                .build();
        try {
            var sentMessage = telegramClient.execute(sendMessage);
            ContextHolder.setLastMessageId(chatId, !message.isNeedRewriting() ? 0 : sentMessage.getMessageId());
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
            var sentMessage = telegramClient.execute(sendDocument);
            ContextHolder.setLastMessageId(chatId, !message.isNeedRewriting() ? 0 : sentMessage.getMessageId());
        } catch (TelegramApiException ex) {
            log.severe("Не удалось отправить документ: " + ex.getMessage());
        }
    }

    private static void editMessage(MessageToUser message, Integer messageId, long chatId) {
        var editMessage = EditMessageText.builder()
                .chatId(String.valueOf(chatId))
                .messageId(messageId)
                .text(message.getText())
                .replyMarkup((InlineKeyboardMarkup) message.getKeyboardMarkup())
                .build();
        try {
            telegramClient.execute(editMessage);
            ContextHolder.setLastMessageId(chatId, !message.isNeedRewriting() ? 0 : messageId);
        } catch (TelegramApiException ex) {
            if (message.getDocument() != null) {
                sendDocument(message, chatId);
            } else {
                sendMessage(message, chatId);
            }
        }
    }
}