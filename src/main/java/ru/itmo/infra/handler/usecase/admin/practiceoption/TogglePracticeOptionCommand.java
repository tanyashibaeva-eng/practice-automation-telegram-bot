package ru.itmo.infra.handler.usecase.admin.practiceoption;

import lombok.SneakyThrows;
import ru.itmo.application.PracticeOptionService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.exception.BadRequestException;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;
import ru.itmo.util.TextUtils;

public class TogglePracticeOptionCommand implements AdminCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var fields = TextUtils.removeRedundantSpaces(message.getText()).split(" +");
        if (fields.length < 2) {
            throw new BadRequestException("Формат: /practice_option_toggle <id>");
        }
        long id = TextUtils.parseDoubleStrToLong(fields[1]);
        var options = PracticeOptionService.getAllOptions();
        var optionOpt = options.stream().filter(o -> o.getId() == id).findFirst();
        if (optionOpt.isEmpty()) {
            throw new BadRequestException("Вариант с id=%d не найден".formatted(id));
        }
        if (optionOpt.get().isEnabled()) {
            PracticeOptionService.disableOption(id);
            return MessageToUser.builder().text("Вариант id=%d выключен".formatted(id)).build();
        }
        PracticeOptionService.enableOption(id);
        return MessageToUser.builder().text("Вариант id=%d включен".formatted(id)).build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/practice_option_toggle";
    }

    @Override
    public String getDescription() {
        return "Вкл/выкл место практики: /practice_option_toggle <id>";
    }
}
