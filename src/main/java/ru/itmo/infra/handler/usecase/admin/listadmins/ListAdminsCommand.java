package ru.itmo.infra.handler.usecase.admin.listadmins;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.TelegramUserService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

import java.util.stream.Collectors;

public class ListAdminsCommand implements AdminCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var admins = TelegramUserService.getAllAdmins();

        String result = "Администраторы:\n\n"
                + admins.stream()
                .map(user -> "Пользователь @%s, chatId: %d, забанен: %s".formatted(user.getUsername(), user.getChatId(), user.isBanned() ? "Да" : "Нет"))
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
        return "/list_admins";
    }

    @Override
    public String getDescription() {
        return "Получить список администраторов";
    }
}
