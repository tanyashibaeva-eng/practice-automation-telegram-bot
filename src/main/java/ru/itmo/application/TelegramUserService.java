package ru.itmo.application;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TelegramUserService {
    /*                         <chatId, Status> */
    private final ConcurrentMap<Long, String> statusMap = new ConcurrentHashMap<>();

}
