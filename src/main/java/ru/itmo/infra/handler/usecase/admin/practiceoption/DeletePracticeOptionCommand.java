package ru.itmo.infra.handler.usecase.admin.practiceoption;

import lombok.SneakyThrows;
import ru.itmo.application.PracticeOptionService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.exception.BadRequestException;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;
import ru.itmo.util.TextUtils;

public class DeletePracticeOptionCommand implements AdminCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var fields = TextUtils.removeRedundantSpaces(message.getText()).split(" +");
        if (fields.length < 2) {
            throw new BadRequestException("Формат: /practice_option_delete <id>");
        }
        long id = TextUtils.parseDoubleStrToLong(fields[1]);
        PracticeOptionService.deleteOption(id);
        return MessageToUser.builder()
                .text("Вариант id=%d удален".formatted(id))
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/practice_option_delete";
    }

    @Override
    public String getDescription() {
        return "Удалить место практики: /practice_option_delete <id>";
    }
}
