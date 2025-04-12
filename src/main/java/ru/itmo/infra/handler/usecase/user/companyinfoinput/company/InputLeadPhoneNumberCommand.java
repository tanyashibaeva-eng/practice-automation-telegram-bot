package ru.itmo.infra.handler.usecase.user.companyinfoinput.company;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.CompanyInfoUpdateArgs;
import ru.itmo.exception.BadRequestException;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.util.TextParser;

public class InputLeadPhoneNumberCommand implements Command {
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
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        try {
            TextParser.parsePhone(phone);
        } catch (BadRequestException e) {
            return false;
        }
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        return digitsOnly.startsWith("+7") || digitsOnly.startsWith("8");
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }
}
