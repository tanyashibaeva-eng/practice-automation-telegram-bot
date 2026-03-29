package ru.itmo.bot;

import lombok.Getter;
import lombok.extern.java.Log;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
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
            var cq = update.getCallbackQuery();
            MaybeInaccessibleMessage cbMessage = cq.getMessage();
            if (cbMessage == null) {
                try {
                    telegramClient.execute(AnswerCallbackQuery.builder()
                            .callbackQueryId(cq.getId())
                            .text("")
                            .build());
                } catch (TelegramApiException ex) {
                    log.severe("answerCallbackQuery (no message): " + ex.getMessage());
                }
                return;
            }
            Long resolvedChatId = cbMessage.getChatId();
            Integer resolvedMessageId = cbMessage.getMessageId();
            if (resolvedChatId == null || resolvedMessageId == null) {
                try {
                    telegramClient.execute(AnswerCallbackQuery.builder()
                            .callbackQueryId(cq.getId())
                            .text("")
                            .build());
                } catch (TelegramApiException ex) {
                    log.severe("answerCallbackQuery (no chat/message id): " + ex.getMessage());
                }
                return;
            }
            var chat = cbMessage.getChat();
            String username = chat != null ? chat.getUserName() : null;
            String callbackDataString = cq.getData();
            chatId = resolvedChatId;
            int callbackSourceMessageId = resolvedMessageId;
            MessageDTO messageDTO = MessageDTO.builder()
                    .chatId(chatId)
                    .username(username)
                    .text(callbackDataString)
                    .build();
            response = Interceptor.processCallback(messageDTO, callbackDataString);

            isCallback = !(response.getKeyboardMarkup() instanceof ReplyKeyboardMarkup || response.getFileStream() != null);
            sendToUser(
                    response,
                    chatId,
                    isCallback,
                    cq.getId(),
                    callbackSourceMessageId);
            clearKeyboardMessage(chatId);
            return;
        }
        if (update.hasMessage()) {
            String username = (update.getMessage() == null) ? null : update.getMessage().getChat().getUserName();
            Message message = update.getMessage();
            chatId = message.getChatId();
            MessageDTO messageDTO = MessageDTO.builder()
                    .chatId(chatId)
                    .username(username)
                    .text(message.getText())
                    .document(message.getDocument())
                    .entities(message.hasText() ? message.getEntities() : null)
                    .build();
            response = Interceptor.processMessage(messageDTO);
        }

        sendToUser(response, chatId, false, null, null);
        clearKeyboardMessage(chatId);
    }

    public static void sendToUser(MessageToUser response, long chatId, boolean isCallback, String callbackQueryId) {
        sendToUser(response, chatId, isCallback, callbackQueryId, null);
    }

    /**
     * @param callbackSourceMessageId id сообщения, с которого пришёл inline-callback — им правим вместо
     *                                  {@link ContextHolder#getLastMessageId}, чтобы не затирать чужое сообщение
     *                                  при гонках или устаревшем last id.
     */
    public static void sendToUser(
            MessageToUser response,
            long chatId,
            boolean isCallback,
            String callbackQueryId,
            Integer callbackSourceMessageId) {
        if (response == null) {
            return;
        }

        if (isCallback && callbackQueryId != null) {
            try {
                String ans = response.getCallbackAnswerText();
                String text = ans == null ? "" : ans;
                if (text.length() > 200) {
                    text = text.substring(0, 197) + "…";
                }
                telegramClient.execute(AnswerCallbackQuery.builder()
                        .callbackQueryId(callbackQueryId)
                        .text(text)
                        .build());
            } catch (TelegramApiException ex) {
                log.severe("answerCallbackQuery: " + ex.getMessage());
            }
        }

        if (isCallback && response.isSkipMessageEdit()) {
            return;
        }

        if (isCallback) {
            int targetId = callbackSourceMessageId != null ? callbackSourceMessageId : ContextHolder.getLastMessageId(chatId);
            if (targetId <= 0) {
                sendMessage(response, chatId);
            } else {
                editMessage(response, targetId, chatId);
            }
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

        var sendBuilder = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(message.getText())
                .replyMarkup(message.getKeyboardMarkup());
        if (message.getParseMode() != null && !message.getParseMode().isBlank()) {
            sendBuilder.parseMode(message.getParseMode());
        }
        var sendMessage = sendBuilder.build();
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
        var editBuilder = EditMessageText.builder()
                .chatId(String.valueOf(chatId))
                .messageId(messageId)
                .text(message.getText())
                .replyMarkup((InlineKeyboardMarkup) message.getKeyboardMarkup());
        if (message.getParseMode() != null && !message.getParseMode().isBlank()) {
            editBuilder.parseMode(message.getParseMode());
        }
        var editMessage = editBuilder.build();
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