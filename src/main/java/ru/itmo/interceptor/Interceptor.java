package ru.itmo.interceptor;

import lombok.extern.java.Log;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.itmo.infra.handler.Handler;

import java.io.IOException;

@Log
public class Interceptor {
    public static String intercept(Message message) {
        try {
            return Handler.handleMessage(message);
        } catch (TelegramApiException | IOException ex) {
            log.severe(ex.getMessage());
            return "Что-то пошло не так";
        }
    }
}
