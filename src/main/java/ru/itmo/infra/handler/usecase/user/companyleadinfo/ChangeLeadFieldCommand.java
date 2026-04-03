package ru.itmo.infra.handler.usecase.user.companyleadinfo;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.Command;

public class ChangeLeadFieldCommand implements Command {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var callbackData = new CallbackData(message.getText());
        var fieldName = callbackData.getValue();
        var field = LeadInfoField.fromString(fieldName);

        if (field == null) {
            ContextHolder.endCommand(chatId);
            return MessageToUser.builder()
                    .text("Неизвестное поле для изменения")
                    .keyboardMarkup(Command.returnToStartInlineMarkup())
                    .needRewriting(true)
                    .build();
        }

        ContextHolder.setCommandData(chatId, field);
        ContextHolder.setNextCommand(chatId, new ChangeLeadFieldInputCommand());

        String prompt = switch (field) {
            case FULLNAME -> "Введите новое ФИО руководителя (Фамилия Имя Отчество):";
            case LASTNAME -> "Введите новую фамилию руководителя:";
            case FIRSTNAME -> "Введите новое имя руководителя:";
            case PATRONYMIC -> "Введите новое отчество руководителя:";
            case PHONE -> "Введите новый номер телефона руководителя:";
            case EMAIL -> "Введите новый email руководителя:";
            case JOB_TITLE -> "Введите новую должность руководителя:";
        };

        return MessageToUser.builder()
                .text(prompt)
                .keyboardMarkup(Command.returnToStartInlineMarkup())
                .needRewriting(true)
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/change_lead_field";
    }
}
