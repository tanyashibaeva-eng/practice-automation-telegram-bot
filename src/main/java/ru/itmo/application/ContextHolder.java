package ru.itmo.application;

import lombok.AllArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.exception.UnknownUserException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class ContextHolder {
    private static final ConcurrentMap<Long, Map<ContextHolderType, Object>> contextMap = new ConcurrentHashMap<>();

    public void removeChatId(Long chatId) {
        contextMap.remove(chatId);
    }

    public Function<MessageDTO, MessageToUser> getNextFunction(long chatId) throws UnknownUserException {
        if (contextMap.containsKey(chatId)) {
            return (Function<MessageDTO, MessageToUser>) contextMap.get(chatId).get(ContextHolderType.FUNCTION);
        }
        throw new UnknownUserException(chatId);
    }

    public void setNextFunction(Long chatId, Function<MessageDTO, MessageToUser> handler) {
        if (!contextMap.containsKey(chatId)) {
            contextMap.put(chatId, new HashMap<>());
        }
        contextMap.get(chatId).put(ContextHolderType.FUNCTION, handler);
    }

    public String getEduStreamName(long chatId) throws UnknownUserException {
        try {
            if (contextMap.containsKey(chatId)) {
                return (String) contextMap.get(chatId).get(ContextHolderType.EDU_STREAM_ID);
            }
        } catch (Exception ignored) {}
        throw new UnknownUserException(chatId);
    }

    public void setEduStreamName(Long chatId, String streamName) throws UnknownUserException {
        if (!contextMap.containsKey(chatId)) {
            contextMap.put(chatId, new HashMap<>());
        }
        contextMap.get(chatId).put(ContextHolderType.EDU_STREAM_ID, streamName);
    }
}

@AllArgsConstructor
enum ContextHolderType {
    FUNCTION,
    EDU_STREAM_ID,
}

