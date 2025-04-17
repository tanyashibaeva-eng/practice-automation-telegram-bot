package ru.itmo.infra.handler.usecase.user.companyinfoinput.company;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.CompanyInfoUpdateArgs;
import ru.itmo.infra.handler.usecase.user.UserCommand;

public class CompanyInfoSummaryCommand implements UserCommand {
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var dto = (CompanyInfoUpdateArgs) ContextHolder.getCommandData(chatId);
        ContextHolder.setNextCommand(chatId, new CompanyInfoConfirmationCommand());
        return MessageToUser.builder()
                .text(("""
                        Вы будете проходить практику в компании: %s, ИНН %d.
                        Формат прохождения: %s.
                        Ваш руководитель практики: %s.\s
                        Данные руководителя: должность: %s, номер телефона: %s, корпоративная почта %s. Верно?""")
                        .formatted(dto.getCompanyName(), dto.getInn(), dto.getPracticeFormat().getDisplayName(), dto.getCompanyLeadFullName(), dto.getCompanyLeadJobTitle(), dto.getCompanyLeadPhone(), dto.getCompanyLeadEmail() ))
                .keyboardMarkup(getInlineKeyboard())
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }
}
