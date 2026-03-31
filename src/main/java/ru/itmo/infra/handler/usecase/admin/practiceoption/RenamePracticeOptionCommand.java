package ru.itmo.infra.handler.usecase.admin.practiceoption;

import lombok.SneakyThrows;
import ru.itmo.application.PracticeOptionService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.exception.BadRequestException;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;
import ru.itmo.util.TextUtils;

public class RenamePracticeOptionCommand implements AdminCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var text = TextUtils.removeRedundantSpaces(message.getText());
        var parts = text.split(" +");
        if (parts.length < 3) {
            throw new BadRequestException("Формат: /practice_option_rename <id> <новое название>");
        }
        long id = TextUtils.parseDoubleStrToLong(parts[1]);
        var title = text.replaceFirst("^/practice_option_rename\\s+\\S+\\s*", "");
        if (title.isBlank()) {
            throw new BadRequestException("Новое название не может быть пустым");
        }
        PracticeOptionService.renameOption(id, title);
        return MessageToUser.builder()
                .text("Вариант id=%d переименован".formatted(id))
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/practice_option_rename";
    }

    @Override
    public String getDescription() {
        return "Переименовать: /practice_option_rename <id> <название>";
    }
}
