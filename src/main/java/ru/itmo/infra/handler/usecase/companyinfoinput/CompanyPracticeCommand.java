package ru.itmo.infra.handler.usecase.companyinfoinput;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.Command;

public class CompanyPracticeCommand implements Command {
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        ContextHolder.setNextCommand(chatId, new InfoTakenCommand());

        return MessageToUser.builder()
                .text("Введите ИНН компании")
                .build();
    }
    // TODO написать логику вывода сообщения об ошибке (если данные не валидны)
    //  и еще страницу о том если компании нет в списке с подписанными договорами

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/where_company";
    }
}
