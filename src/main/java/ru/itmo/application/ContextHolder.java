package ru.itmo.application;

import lombok.AllArgsConstructor;
import ru.itmo.exception.UnknownUserException;
import ru.itmo.infra.handler.usecase.Command;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ContextHolder {
    private static final ConcurrentMap<Long, Map<ContextHolderType, Object>> contextMap = new ConcurrentHashMap<>();

    public void removeChatId(Long chatId) {
        contextMap.remove(chatId);
    }

    public Command getNextCommand(long chatId) throws UnknownUserException {
        if (contextMap.containsKey(chatId)) {
            return (Command) contextMap.get(chatId).get(ContextHolderType.COMMAND);
        }
        throw new UnknownUserException(chatId);
    }

    public void setNextCommand(Long chatId, Command command) {
        if (!contextMap.containsKey(chatId)) {
            contextMap.put(chatId, new HashMap<>());
        }
        contextMap.get(chatId).put(ContextHolderType.COMMAND, command);
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
    COMMAND,
    EDU_STREAM_ID,
}

