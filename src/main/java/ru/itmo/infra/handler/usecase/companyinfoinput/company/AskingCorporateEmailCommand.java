package ru.itmo.infra.handler.usecase.companyinfoinput.company;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.Command;

    public class AskingCorporateEmailCommand implements Command {
        @SneakyThrows
        public MessageToUser execute(MessageDTO message) {
            var chatId = message.getChatId();
            ContextHolder.setNextCommand(chatId, new InputCorporateEmailCommand());
            return MessageToUser.builder()
                    .text("Введите корпоративную научного руководителя")
                    .keyboardMarkup(getReturnToStartMarkup())
                    .needRewriting(true)
                    .build();
        }

        @Override
        public boolean isNextCallNeeded() {
            return false;
        }

        @Override
        public String getName() {
            return "";
        }
    }
