package ru.itmo.infra.handler.usecase.companyinfoinput.company;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.Command;

public class AskingApproveNoContractCompanyCommand implements Command {

    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        ContextHolder.setNextCommand(chatId, new InputApproveNoContractCompanyCommand());
        return MessageToUser.builder()
                .text("С данной компанией не подписан договор. Вы уверенны, что будете проходить практику в этой компании?")
                .keyboardMarkup(getConfirmationKeyboard())
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/contract_with_company";
    }
}