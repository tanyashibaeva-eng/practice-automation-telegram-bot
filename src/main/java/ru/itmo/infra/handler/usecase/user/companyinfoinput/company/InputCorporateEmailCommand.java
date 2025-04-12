package ru.itmo.infra.handler.usecase.user.companyinfoinput.company;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.CompanyInfoUpdateArgs;
import ru.itmo.infra.handler.usecase.Command;

public class InputCorporateEmailCommand implements Command {
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var email = message.getText().trim();

        if (!isValidCorporateEmail(email)) {
            ContextHolder.setNextCommand(chatId, this);
            return MessageToUser.builder()
                    .text("Некорректный формат email")
                    .keyboardMarkup(getReturnToStartMarkup())
                    .needRewriting(true)
                    .build();
        }

        var dto = (CompanyInfoUpdateArgs) ContextHolder.getCommandData(chatId);
        dto.setCompanyLeadEmail(email);
        ContextHolder.setCommandData(chatId, dto);
        ContextHolder.setNextCommand(chatId, new CompanyInfoSummaryCommand());
        return MessageToUser.builder()
                .text("")
                .build();
    }

    private boolean isValidCorporateEmail(String email) {
        if (email == null) {
            return false;
        }
        return !email.matches(".*@(gmail|yahoo|yandex|mail|outlook|hotmail)\\..*");
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "";
    }
}
