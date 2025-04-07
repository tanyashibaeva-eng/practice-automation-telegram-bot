package ru.itmo.application;

import lombok.AllArgsConstructor;
import ru.itmo.exception.UnknownUserException;
import ru.itmo.infra.handler.usecase.Command;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@AllArgsConstructor
enum ContextHolderType {
    COMMAND,
    COMMAND_DATA,
    EDU_STREAM_NAME,
}

public class ContextHolder {
    private static final ConcurrentMap<Long, Map<ContextHolderType, Object>> contextMap = new ConcurrentHashMap<>();

    public static void endCommand(Long chatId) {
        contextMap.remove(chatId);
    }

    public static Command getNextCommand(long chatId) throws UnknownUserException {
        if (contextMap.containsKey(chatId)) {
            return (Command) contextMap.get(chatId).get(ContextHolderType.COMMAND);
        }
        throw new UnknownUserException(chatId);
    }

    public static void setNextCommand(Long chatId, Command command) {
        if (!contextMap.containsKey(chatId)) {
            contextMap.put(chatId, new HashMap<>());
        }
        contextMap.get(chatId).put(ContextHolderType.COMMAND, command);
    }

    public static String getEduStreamName(long chatId) throws UnknownUserException {
        try {
            if (contextMap.containsKey(chatId)) {
                return (String) contextMap.get(chatId).get(ContextHolderType.EDU_STREAM_NAME);
            }
        } catch (Exception ignored) {
        }
        throw new UnknownUserException(chatId);
    }

    public static void setEduStreamName(Long chatId, String streamName) {
        if (!contextMap.containsKey(chatId)) {
            contextMap.put(chatId, new HashMap<>());
        }
        contextMap.get(chatId).put(ContextHolderType.EDU_STREAM_NAME, streamName);
    }

    public static Object getCommandData(long chatId) throws UnknownUserException {
        try {
            if (contextMap.containsKey(chatId)) {
                return contextMap.get(chatId).get(ContextHolderType.COMMAND_DATA);
            }
        } catch (Exception ignored) {
        }
        throw new UnknownUserException(chatId);
    }

    public static void setCommandData(Long chatId, Object commandData) {
        if (!contextMap.containsKey(chatId)) {
            contextMap.put(chatId, new HashMap<>());
        }
        contextMap.get(chatId).put(ContextHolderType.COMMAND_DATA, commandData);
    }
}

