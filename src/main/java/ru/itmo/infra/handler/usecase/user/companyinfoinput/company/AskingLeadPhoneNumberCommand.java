package ru.itmo.infra.handler.usecase.user.companyinfoinput.company;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.Command;

    public class AskingLeadPhoneNumberCommand implements Command {
        @SneakyThrows
        public MessageToUser execute(MessageDTO message) {
            var chatId = message.getChatId();
            ContextHolder.setNextCommand(chatId, new InputLeadPhoneNumberCommand());
            return MessageToUser.builder()
                    .text("Введите номер телефона руководителя практики от компании (Например: +7 917 154 60 71)")
                    .keyboardMarkup(getReturnToStartMarkup())
                    .needRewriting(true)
                    .build();
        }

        @Override
        public boolean isNextCallNeeded() {
            return false;
        }
    }
