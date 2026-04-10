package ru.itmo.infra.handler.usecase.admin.practiceoption;

import lombok.SneakyThrows;
import ru.itmo.application.PracticeOptionService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

public class ListPracticeOptionsCommand implements AdminCommand {
    private static final String COMMAND_NAME = "/practice_option_list";

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var options = PracticeOptionService.getAllOptions();
        var text = new StringBuilder("""
                Места практики и флаги

                Обозначения:
                - ITMO ✅: после выбора запрашиваются данные по практике в ИТМО.
                - Company ✅: после выбора запрашиваются данные компании.
                - Оба ❌: после выбора доп. данные не запрашиваются.

                """);
        if (options.isEmpty()) {
            text.append("Список пока пуст.");
        } else {
            for (var option : options) {
                text.append("#").append(option.getId()).append(" ").append(option.getTitle()).append("\n")
                        .append("Вкл: ").append(option.isEnabled() ? "✅" : "❌")
                        .append(" | ITMO: ").append(option.isRequiresItmoInfo() ? "✅" : "❌")
                        .append(" | Company: ").append(option.isRequiresCompanyInfo() ? "✅" : "❌")
                        .append("\n\n");
            }
        }
        return MessageToUser.builder()
                .text(text.toString().trim())
                .keyboardMarkup(getReturnToStartMarkup())
                .needRewriting(true)
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return "Показать все места практики";
    }
}
