package ru.itmo.infra.handler.usecase.admin.practiceoption;

import lombok.SneakyThrows;
import ru.itmo.application.PracticeOptionService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.exception.BadRequestException;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

public class AddPracticeOptionCommand implements AdminCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var text = message.getText() == null ? "" : message.getText().trim();
        var payload = text.replaceFirst("^/practice_option_add\\s*", "");
        if (payload.isBlank()) {
            throw new BadRequestException("Формат: /practice_option_add <itmo|company|none> <название>");
        }
        var parts = payload.split(" +", 2);
        String mode;
        String title;
        if (parts.length == 1) {
            mode = "company";
            title = parts[0];
        } else {
            mode = parts[0].trim().toLowerCase();
            title = parts[1].trim();
        }
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
            default -> throw new BadRequestException("Первый аргумент: itmo, company или none");
        }
        var option = PracticeOptionService.addOption(title, requiresItmoInfo, requiresCompanyInfo);
        return MessageToUser.builder()
                .text("Добавлен вариант: id=%d, title=%s".formatted(option.getId(), option.getTitle()))
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/practice_option_add";
    }

    @Override
    public String getDescription() {
        return "Добавить место: /practice_option_add <itmo|company|none> <название>";
    }
}
