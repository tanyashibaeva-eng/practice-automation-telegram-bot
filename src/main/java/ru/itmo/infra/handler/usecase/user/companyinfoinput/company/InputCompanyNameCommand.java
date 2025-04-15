package ru.itmo.infra.handler.usecase.user.companyinfoinput.company;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.CompanyInfoUpdateArgs;
import ru.itmo.infra.handler.usecase.user.UserCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.itmo.AskingITMOPracticeLeadFullNameCommand;

public class InputCompanyNameCommand implements UserCommand {
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var companyName = message.getText().trim();
        if (!isValidCompanyName(companyName)) {
            return MessageToUser.builder()
                    .text("Не корректный ввод")
                    .keyboardMarkup(getReturnToStartMarkup())
                    .needRewriting(true)
                    .build();
        }

        var dto = (CompanyInfoUpdateArgs) ContextHolder.getCommandData(chatId);
        dto.setCompanyName(companyName);
        ContextHolder.setCommandData(chatId, dto);
        ContextHolder.setNextCommand(chatId, new AskingITMOPracticeLeadFullNameCommand());
        return MessageToUser.builder()
                .text("")
                .keyboardMarkup(getInlineKeyboard())
                .build();
    }

    private boolean isValidCompanyName(String companyName) {
        return companyName != null && !companyName.trim().isEmpty();
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }
}
