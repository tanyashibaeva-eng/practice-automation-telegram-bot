package ru.itmo.infra.handler.usecase.admin.practiceoption;

import lombok.SneakyThrows;
import ru.itmo.application.PracticeOptionService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

public class ListPracticeOptionsCommand implements AdminCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        return MessageToUser.builder()
                .text(PracticeOptionService.optionsAsTextTable())
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/practice_option_list";
    }

    @Override
    public String getDescription() {
        return "Показать все места практики";
    }
}
