package ru.itmo.interceptor;

import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.exception.InvalidMessageException;
import ru.itmo.infra.handler.Handler;

import java.io.IOException;

@Log
public class Interceptor {
    @SneakyThrows
    public static String intercept(Message message) {
        try {
            return Handler.handleMessage(message);
        } catch (InvalidMessageException | BadRequestException e) {
            return e.getMessage();
        } catch (InternalException e) {
            log.severe(e.getCause().getMessage());
            throw new RuntimeException(e.getMessage());
        } catch (TelegramApiException | IOException ex) {
            log.severe(ex.getMessage());
            return "Что-то пошло не так";
        }
    }
}
