package ru.itmo.infra.handler;

import org.telegram.telegrambots.meta.api.objects.message.Message;

public class GreetingCommand {
    public static String greetingAdminCommand(Message message) {
        return "Привет, ты на стартовой странице, тут будут кнопочки для навигации!";
    }

    public static String end(Message message) {
        return "";
    }
}
