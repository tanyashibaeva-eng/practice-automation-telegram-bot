package ru.itmo.infra.handler.usecase.user.companyinfoinput.company;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.CompanyInfoUpdateArgs;
import ru.itmo.infra.handler.usecase.user.UserCommand;

public class InputLeadJobTitleCommand implements UserCommand {
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var leadJob = message.getText().trim();
        if (!isValidLeadPost(leadJob)) {
            return MessageToUser.builder()
                    .text("Не корректный ввод")
                    .keyboardMarkup(getReturnToStartMarkup())
                    .needRewriting(true)
                    .build();
        }

        var dto = (CompanyInfoUpdateArgs) ContextHolder.getCommandData(chatId);
        dto.setCompanyLeadJobTitle(leadJob);
        ContextHolder.setCommandData(chatId, dto);
        ContextHolder.setNextCommand(chatId, new AskingLeadPhoneNumberCommand());
        return MessageToUser.builder()
                .text("")
                .keyboardMarkup(getInlineKeyboard())
                .build();
    }

    private boolean isValidLeadPost(String leadJob) {
        return leadJob != null && !leadJob.trim().isEmpty();
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }
}
