package ru.itmo.infra.handler;

import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;

public class GreetingCommand {
    public static MessageToUser greetingAdminCommand(MessageDTO message) {
        return  MessageToUser.builder().text("Привет, ты на стартовой странице, тут будут кнопочки для навигации!").build();
    }

    public static MessageToUser end(MessageDTO message) {
        return  MessageToUser.builder().text("").build();
    }
}
