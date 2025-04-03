package ru.itmo.application;

import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.UnknownUserException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class TelegramUserService {
    private static final ConcurrentMap<Long, Function<Message, String>> currentCommandFunctionMap = new ConcurrentHashMap<>();

    public void setNextFunction(Long chatId, Function<Message, String> handler) {
        currentCommandFunctionMap.put(chatId, handler);
    }

    public void removeChatID(Long chatId) {
        currentCommandFunctionMap.remove(chatId);
    }

    public Function<Message, String> getNextFunction(Long chatId) throws UnknownUserException {
        if (currentCommandFunctionMap.containsKey(chatId)) {
            return currentCommandFunctionMap.get(chatId);
        }
        throw new UnknownUserException(chatId);
    }
}
