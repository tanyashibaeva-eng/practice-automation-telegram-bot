package ru.itmo.application;

import lombok.AllArgsConstructor;
import ru.itmo.exception.UnknownUserException;
import ru.itmo.infra.handler.usecase.Command;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ContextHolder {
    private static final ConcurrentMap<Long, Map<ContextHolderType, Object>> contextMap = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Long, Integer> lastMessageIdMap = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Long, Integer> prevCallbackMessageId = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Long, Integer> currCallbackMessageId = new ConcurrentHashMap<>();

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
        contextMap.computeIfAbsent(chatId, k -> new ConcurrentHashMap<>())
                .put(ContextHolderType.COMMAND, command);
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
        contextMap.computeIfAbsent(chatId, k -> new ConcurrentHashMap<>())
                .put(ContextHolderType.EDU_STREAM_NAME, streamName);
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
        contextMap.computeIfAbsent(chatId, k -> new ConcurrentHashMap<>())
                .put(ContextHolderType.COMMAND_DATA, commandData);
    }

    public static Integer getLastMessageId(long chatId) {
        if (lastMessageIdMap.containsKey(chatId)) {
            return lastMessageIdMap.get(chatId);
        }
        return 0;
    }

    public static void setLastMessageId(Long chatId, Integer lastMessageId) {
        lastMessageIdMap.put(chatId, lastMessageId);
        if (lastMessageId == 0) {
            return;
        }

        currCallbackMessageId.put(chatId, lastMessageId);
    }

    public static void setCurrCallbackMessageId(Long chatId, Integer lastMessageId) {
        var curr = ContextHolder.getCurrCallbackId(chatId);
        if (lastMessageId == 0) {
            return;
        }
        if (curr != lastMessageId) {
            prevCallbackMessageId.put(chatId, curr);
        }
        currCallbackMessageId.put(chatId, lastMessageId);
    }

    public static int getPrevCallbackId(long chatId) {
        if (prevCallbackMessageId.containsKey(chatId)) {
            return prevCallbackMessageId.get(chatId);
        }
        return 0;
    }

    public static int getCurrCallbackId(long chatId) {
        if (currCallbackMessageId.containsKey(chatId)) {
            return currCallbackMessageId.get(chatId);
        }
        return 0;
    }
}


@AllArgsConstructor
enum ContextHolderType {
    COMMAND,
    COMMAND_DATA,
    EDU_STREAM_NAME,
    LAST_MESSAGE_ID,
}

