package ru.itmo.bot;

import lombok.Getter;
import lombok.extern.java.Log;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
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

    @Getter
    private static final TelegramClient telegramClient = new OkHttpTelegramClient(PropertiesProvider.getToken());

    @Override
    public void consume(Update update) {
        MessageToUser response = null;
        long chatId = 0;
        boolean isCallback = false;

        if (update.hasCallbackQuery()) {
            String username = (update.getCallbackQuery().getMessage() == null) ? null : update.getCallbackQuery().getMessage().getChat().getUserName();
            String callbackDataString = update.getCallbackQuery().getData();
            chatId = update.getCallbackQuery().getMessage().getChatId();
            MessageDTO messageDTO = MessageDTO.builder()
                    .chatId(chatId)
                    .username(username)
                    .text(callbackDataString)
                    .build();
            response = Interceptor.processCallback(messageDTO, callbackDataString);

            isCallback = !(response.getKeyboardMarkup() instanceof ReplyKeyboardMarkup || response.getFileStream() != null);
        }
        if (update.hasMessage()) {
            String username = (update.getMessage() == null) ? null : update.getMessage().getChat().getUserName();
            Message message = update.getMessage();
            chatId = message.getChatId();

            String photoFileId = null;
            if (message.hasPhoto() && !message.getPhoto().isEmpty()) {
                var photos = message.getPhoto();
                photoFileId = photos.get(photos.size() - 1).getFileId();
            }

            MessageDTO messageDTO = MessageDTO.builder()
                    .chatId(chatId)
                    .username(username)
                    .text(message.getText())
                    .document(message.getDocument())
                    .photoFileId(photoFileId)
                    .build();
            response = Interceptor.processMessage(messageDTO);
        }

        sendToUser(response, chatId, isCallback);
        clearKeyboardMessage(chatId);
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

        if (response.getFileStream() == null) {
            sendMessage(response, chatId);
        } else {
            sendDocument(response, chatId);
        }
    }

    private static void sendMessage(MessageToUser message, long chatId) {
        if (message.getText() != null && message.getText().isBlank()) {
            return;
        }

        var sendMessage = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(message.getText())
                .replyMarkup(message.getKeyboardMarkup())
//                .parseMode("MarkdownV2") // TODO: разобраться какой mode лучше использовать и зарефакторить
                .build();
        try {
            var sentMessage = telegramClient.execute(sendMessage);
            updateMessageIds(message, chatId, sentMessage.getMessageId());
        } catch (TelegramApiException ex) {
            log.severe("Не удалось отправить сообщение: " + ex.getMessage());
        }
    }

    private static void sendDocument(MessageToUser message, long chatId) {
        SendDocument sendDocument = SendDocument.builder()
                .chatId(chatId)
                .document(new InputFile(message.getFileStream(), message.getFileName()))
                .caption(message.getText())
                .replyMarkup(message.getKeyboardMarkup())
                .build();
        try {
            var sentMessage = telegramClient.execute(sendDocument);
            updateMessageIds(message, chatId, sentMessage.getMessageId());
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
            updateMessageIds(message, chatId, editMessage.getMessageId());
        } catch (TelegramApiException ex) {
            if (message.getFileStream() != null) {
                sendDocument(message, chatId);
            } else {
                sendMessage(message, chatId);
            }
        }
    }

    private static void updateMessageIds(MessageToUser message, Long chatId, Integer messageId) {
        if (message.getKeyboardMarkup() != null && message.getKeyboardMarkup() instanceof InlineKeyboardMarkup) {
            ContextHolder.setCurrCallbackMessageId(chatId, messageId);
        }
        ContextHolder.setLastMessageId(chatId, !message.isNeedRewriting() ? 0 : messageId);
    }

    private static void clearKeyboardMessage(long chatId) {
        var prev = ContextHolder.getPrevCallbackId(chatId);
        var curr = ContextHolder.getLastMessageId(chatId);
        if (prev == 0 && curr == 0) {
            return;
        }
        if (prev == curr || prev == 0) {
            return;
        }
        var editMessage = EditMessageReplyMarkup.builder()
                .chatId(String.valueOf(chatId))
                .messageId(prev)
                .replyMarkup(null)
                .build();
        try {
            telegramClient.execute(editMessage);
        } catch (TelegramApiException ex) {
        }
    }
}