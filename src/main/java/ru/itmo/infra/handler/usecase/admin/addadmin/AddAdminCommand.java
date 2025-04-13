package ru.itmo.infra.handler.usecase.admin.addadmin;

import lombok.SneakyThrows;
import ru.itmo.application.AdminTokenService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.model.AdminToken;
import ru.itmo.infra.handler.usecase.Command;

public class AddAdminCommand implements Command {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {

        AdminToken token = AdminTokenService.generateToken();

        return MessageToUser.builder()
                .text("Токен для добавления админа сгенерирован (Не сообщать студентам): " + token.getToken()).build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/generate_token";
    }
}
