package ru.itmo.infra.handler.usecase.help;

import lombok.SneakyThrows;
import ru.itmo.application.AuthorizationService;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.Handler;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

import java.util.ArrayList;

public class HelpCommand implements AdminCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        ContextHolder.endCommand(message.getChatId());
        var text = "\"Справка по командам:\\n\"";
        if (AuthorizationService.canDoAdminActions(message.getChatId())) {
            text += getAdminHelp();
        }

        return MessageToUser.builder()
                .text(text)
                .keyboardMarkup(getReturnToStartMarkup())
                .needRewriting(true)
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/help";
    }

    @Override
    public String getDescription() {
        return "Справка с информацией о командах";
    }

    private String getAdminHelp() {
        var commands = Handler.getAdminCommands();
        var helpMessages = new ArrayList<String>();

        for (var cmd : commands) {
            String name = cmd.getName();
            if (name != null && !name.isBlank()) {
                helpMessages.add("- " + name + ": " + cmd.getDescription() + "\n");
            }
        }

        return String.join("\n", helpMessages);
    }

}
