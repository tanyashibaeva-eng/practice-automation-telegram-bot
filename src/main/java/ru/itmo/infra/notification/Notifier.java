package ru.itmo.infra.notification;

import lombok.extern.java.Log;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.itmo.util.PropertiesProvider;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log
public class Notifier {

    private static final TelegramClient telegramClient = new OkHttpTelegramClient(PropertiesProvider.getToken());
    private static final ScheduledExecutorService schedulingExecutor = Executors.newSingleThreadScheduledExecutor();
    private static final ConcurrentLinkedQueue<Notification> messageQueue = new ConcurrentLinkedQueue<>();

    static {
        schedulingExecutor.scheduleWithFixedDelay(
                Notifier::sendNotification,
                0,
                150,
                TimeUnit.MILLISECONDS
        );
    }

    public static void notifyAsync(Notification notification) {
        notification.setRetryCount(0);
        messageQueue.add(notification);
    }

    private static void sendNotification() {
        if (messageQueue.isEmpty()) return;

        Notification notification = messageQueue.poll();
        if (!notification.countRetry()) return;

        SendMessage sendmessage = SendMessage.builder()
                .chatId(notification.getChatId())
                .text(notification.getText())
                .replyMarkup(notification.getKeyboardMarkup())
//                .parseMode("MarkdownV2")
                .build();

        try {
            telegramClient.execute(sendmessage);
        } catch (TelegramApiException ex) {
            log.warning("Ошибка отправки уведомления: " + ex.getMessage());
            messageQueue.add(notification);
        }
    }
}
