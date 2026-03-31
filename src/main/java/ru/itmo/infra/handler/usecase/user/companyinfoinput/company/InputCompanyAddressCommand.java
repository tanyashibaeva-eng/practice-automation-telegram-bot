package ru.itmo.infra.handler.usecase.user.companyinfoinput.company;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.CompanyInfoUpdateArgs;
import ru.itmo.infra.handler.usecase.user.UserCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.itmo.AskingITMOPracticeLeadFullNameCommand;

public class InputCompanyAddressCommand implements UserCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var companyAddress = message.getText().trim();
        if (companyAddress.isBlank()) {
            ContextHolder.setNextCommand(chatId, this);
            return MessageToUser.builder()
                    .text("Введите адрес компании корректно")
                    .keyboardMarkup(getReturnToStartMarkup())
                    .needRewriting(true)
                    .build();
        }

        var dto = (CompanyInfoUpdateArgs) ContextHolder.getCommandData(chatId);
        dto.setCompanyAddress(companyAddress);
        ContextHolder.setCommandData(chatId, dto);
        ContextHolder.setNextCommand(chatId, dto.isPresentInITMOAgreementFile()
                ? new AskingITMOPracticeLeadFullNameCommand()
                : new AskingApproveNoContractCompanyCommand());
        return MessageToUser.builder()
                .text("")
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }
}
