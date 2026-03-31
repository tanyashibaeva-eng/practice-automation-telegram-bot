package ru.itmo.infra.handler.usecase.user.companyinfoinput.company;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.CompanyInfoUpdateArgs;
import ru.itmo.infra.handler.usecase.user.UserCommand;
import ru.itmo.util.TextUtils;

public class InputLeadPhoneNumberCommand implements UserCommand {
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var phoneNumber = message.getText().trim();

        if (!isValidPhoneNumber(phoneNumber)) {
            ContextHolder.setNextCommand(chatId, this);
            return MessageToUser.builder()
                    .text("Некорректный формат номера")
                    .keyboardMarkup(getReturnToStartMarkup())
                    .needRewriting(true)
                    .build();
        }

        var dto = (CompanyInfoUpdateArgs) ContextHolder.getCommandData(chatId);
        dto.setCompanyLeadPhone(phoneNumber);
        ContextHolder.setCommandData(chatId, dto);
        ContextHolder.setNextCommand(chatId, new AskingCorporateEmailCommand());
        return MessageToUser.builder()
                .text("")
                .build();
    }

    private boolean isValidPhoneNumber(String phone) {
        try {
            String parsed = TextUtils.parsePhone(phone);
            return parsed.startsWith("+7") || parsed.startsWith("8");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }
}
