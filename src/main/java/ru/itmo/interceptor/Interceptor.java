package ru.itmo.interceptor;

import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.itmo.bot.MessageToUser;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.exception.InvalidMessageException;
import ru.itmo.infra.handler.Handler;

import java.io.IOException;

@Log
public class Interceptor {
    @SneakyThrows
    public static MessageToUser processMessage(Message message) {
        try {
            return Handler.handleMessage(message);
        } catch (InvalidMessageException | BadRequestException e) {
            return  MessageToUser.builder().text(e.getMessage()).build();
        } catch (InternalException e) {
            log.severe(e.getCause().getMessage());
            return  MessageToUser.builder().text("Что-то пошло не так").build();
        } catch (TelegramApiException | IOException ex) {
            log.severe(ex.getMessage());
            return  MessageToUser.builder().text("Что-то пошло не так").build();
        }
    }
}
