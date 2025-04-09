package ru.itmo.infra.handler.usecase.studentapplicationinput;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.Command;

public class CodeAndNameSpecializationCommand implements Command {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
// TODO валидация
        ContextHolder.setNextCommand(message.getChatId(), new ApplicationInfoTakenCommand());

        return MessageToUser.builder()
                .text("Введите код и наименование направления вашей специаольности (Например: 09.03.04 Программная инжинерия)")
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/code_amd_name";
    }
}
