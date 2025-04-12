package ru.itmo.infra.handler.usecase.user.companyinfoinput.company;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.CompanyInfoUpdateArgs;
import ru.itmo.infra.handler.usecase.Command;

import java.util.regex.Pattern;

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
        if (phone == null) {
            return false;
        }
        Pattern PHONE_PATTERN = Pattern.compile("^(\\+7|8)[\\s\\-]?\\(?\\d{3}\\)?[\\s\\-]?\\d{3}[\\s\\-]?\\d{2}[\\s\\-]?\\d{2}$");
        return PHONE_PATTERN.matcher(phone).matches();
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
