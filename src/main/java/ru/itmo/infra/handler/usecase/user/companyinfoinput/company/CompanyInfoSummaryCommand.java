package ru.itmo.infra.handler.usecase.user.companyinfoinput.company;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.CompanyInfoUpdateArgs;
import ru.itmo.infra.handler.usecase.user.UserCommand;

public class CompanyInfoSummaryCommand implements UserCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var dto = (CompanyInfoUpdateArgs) ContextHolder.getCommandData(chatId);
        ContextHolder.setNextCommand(chatId, new CompanyInfoConfirmationCommand());

        var addressLine = (dto.getCompanyAddress() == null || dto.getCompanyAddress().isBlank())
                ? ""
                : "Адрес компании: %s.\n".formatted(dto.getCompanyAddress());
        var confirmationLine = dto.isRequiresSpbOfficeApproval()
                ? "Офис компании в Санкт-Петербурге будет отправлен администратору на подтверждение. Верно?"
                : "Верно?";

        return MessageToUser.builder()
                .text("""
                        Вы будете проходить практику в компании: %s, ИНН %d.
                        Формат прохождения: %s.
                        %sВаш руководитель практики: %s.
                        Данные руководителя: должность: %s, номер телефона: %s, корпоративная почта %s.
                        %s
                        """.formatted(
                        dto.getCompanyName(),
                        dto.getInn(),
                        dto.getPracticeFormat().getDisplayName(),
                        addressLine,
                        dto.getCompanyLeadFullName(),
                        dto.getCompanyLeadJobTitle(),
                        dto.getCompanyLeadPhone(),
                        dto.getCompanyLeadEmail(),
                        confirmationLine
                ).trim())
                .keyboardMarkup(getInlineKeyboard())
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }
}
