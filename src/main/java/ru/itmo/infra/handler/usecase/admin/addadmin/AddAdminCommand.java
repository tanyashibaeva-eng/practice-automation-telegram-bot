package ru.itmo.infra.handler.usecase.admin.addadmin;

import lombok.SneakyThrows;
import ru.itmo.application.AdminTokenService;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.model.AdminToken;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

public class AddAdminCommand implements AdminCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        ContextHolder.endCommand(message.getChatId());
        AdminToken token = AdminTokenService.generateToken();
        return MessageToUser.builder()
                .text("Токен для добавления админа сгенерирован (не сообщать студентам): " + token.getToken()).build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "/generate_token";
    }

    @Override
    public String getDescription() {
        return "Сгенерировать токен для администратора";
    }
}
