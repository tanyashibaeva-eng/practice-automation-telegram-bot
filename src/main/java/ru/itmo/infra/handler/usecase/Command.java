package ru.itmo.infra.handler.usecase;

import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;

public interface Command {
    MessageToUser execute(MessageDTO message);
    boolean isTerminal();
    String getName();
}

