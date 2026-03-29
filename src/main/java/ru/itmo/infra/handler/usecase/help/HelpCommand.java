package ru.itmo.infra.handler.usecase.help;

import lombok.SneakyThrows;
import ru.itmo.application.AuthorizationService;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.handler.Handler;
import ru.itmo.infra.handler.usecase.user.UserCommand;
import ru.itmo.infra.storage.GuideRepository;

import java.util.ArrayList;
import java.util.List;

public class HelpCommand implements UserCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        ContextHolder.endCommand(message.getChatId());
        var text = "Справка по командам:\n\n";
        if (AuthorizationService.canDoAdminActions(message.getChatId())) {
            text += getAdminHelp(message.getChatId());
        } else {
            text += getUserHelp(message.getChatId());
        }

        return MessageToUser.builder()
                .text(text)
                .keyboardMarkup(getReturnToStartMarkup())
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "/help";
    }

    @Override
    public String getDescription() {
        return "Справка с информацией о командах";
    }

    private String getAdminHelp(long chatId) {
        var commands = Handler.getAdminCommands(chatId);
        var helpMessages = new ArrayList<String>();

        for (var cmd : commands) {
            String name = cmd.getName();
            if (name != null && !name.isBlank()) {
                helpMessages.add("- " + name + ": " + cmd.getDescription() + "\n");
            }
        }

        return String.join("\n", helpMessages);
    }

    private String getUserHelp(long chatId) {
        StudentStatus status = Handler.getStudentStatus(chatId);
        var commands = Handler.getStudentCommands();
        List<String> helpMessages = new ArrayList<>();

        for (var cmd : commands) {
            if (cmd instanceof UserCommand) {
                String name = cmd.getName();
                if (name != null && !name.isBlank()) {
                    helpMessages.add("- " + name + ": " + cmd.getDescription()+ "\n");
                }
            }
        }
        Handler.getAvailableStudentCommands(status).forEach(cmd -> {
            if (cmd instanceof UserCommand && ((UserCommand) cmd).isAvailableForStatus(status)) {
                helpMessages.add("- " + cmd.getName() + ": " + cmd.getDescription());
            }
        });

        helpMessages.add("");
        helpMessages.add("Разделы мануала:");
        try {
            for (var section : GuideRepository.findAllActiveSectionsOrdered()) {
                helpMessages.add("- " + section.getCommand() + ": " + section.getTitle());
            }
        } catch (InternalException ignored) {
            // список разделов недоступен — пропускаем блок
        }

        return String.join("\n", helpMessages);
    }
}