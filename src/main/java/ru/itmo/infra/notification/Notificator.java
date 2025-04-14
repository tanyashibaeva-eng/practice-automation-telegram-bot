package ru.itmo.infra.notification;

import lombok.extern.java.Log;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.itmo.util.PropertiesProvider;

@Log
public class Notificator {

    private static final TelegramClient telegramClient = new OkHttpTelegramClient(PropertiesProvider.getToken());

    public static void sendNotification(Notification notification) {
        SendMessage sendmessage = SendMessage.builder()
                .chatId(notification.getChatId())
                .text(notification.getText())
                .build();

        try {
            telegramClient.execute(sendmessage);
        } catch (TelegramApiException ex) {
            log.severe("Ошибка отправки уведомления: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
