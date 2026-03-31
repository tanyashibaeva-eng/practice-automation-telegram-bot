package ru.itmo.infra.handler.usecase.admin.practiceoption;

import lombok.SneakyThrows;
import ru.itmo.application.PracticeOptionService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.exception.BadRequestException;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;
import ru.itmo.util.TextUtils;

public class SetPracticeOptionFlagsCommand implements AdminCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var fields = TextUtils.removeRedundantSpaces(message.getText()).split(" +");
        if (fields.length < 3) {
            throw new BadRequestException("Формат: /practice_option_flags <id> <itmo|company|none>");
        }
        long id = TextUtils.parseDoubleStrToLong(fields[1]);
        var mode = fields[2].trim().toLowerCase();

        boolean requiresItmoInfo;
        boolean requiresCompanyInfo;
        switch (mode) {
            case "itmo" -> {
                requiresItmoInfo = true;
                requiresCompanyInfo = false;
            }
            case "none" -> {
                requiresItmoInfo = false;
                requiresCompanyInfo = false;
            }
            case "company" -> {
                requiresItmoInfo = false;
                requiresCompanyInfo = true;
            }
            default -> throw new BadRequestException("Режим должен быть: itmo, company или none");
        }

        PracticeOptionService.updateOptionFlags(id, requiresItmoInfo, requiresCompanyInfo);
        return MessageToUser.builder()
                .text("Флаги для варианта id=%d обновлены: mode=%s".formatted(id, mode))
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/practice_option_flags";
    }

    @Override
    public String getDescription() {
        return "Изменить флаги: /practice_option_flags <id> <itmo|company|none>";
    }
}
