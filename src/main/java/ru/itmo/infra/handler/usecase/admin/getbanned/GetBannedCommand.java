package ru.itmo.infra.handler.usecase.admin.getbanned;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.TelegramUserService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

import java.util.stream.Collectors;

public class GetBannedCommand implements AdminCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var bannedUsers = TelegramUserService.getAllBannedUsers();

        String result = "Забаненные пользователи:\n\n"
                + bannedUsers.stream()
                .map(user -> "Пользователь @%s, chatId: %d".formatted(user.getUsername(), user.getChatId()))
                .collect(Collectors.joining("\n\n"));

        ContextHolder.endCommand(message.getChatId());
        return MessageToUser.builder()
                .text(result)
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "/get_banned";
    }

    @Override
    public String getDescription() {
        return "Получить список забаненных пользователей";
    }
}
