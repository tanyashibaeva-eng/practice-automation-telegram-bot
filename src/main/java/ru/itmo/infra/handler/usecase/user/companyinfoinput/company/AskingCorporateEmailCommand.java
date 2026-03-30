package ru.itmo.infra.handler.usecase.user.companyinfoinput.company;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.user.UserCommand;

public class AskingCorporateEmailCommand implements UserCommand {
        @SneakyThrows
        public MessageToUser execute(MessageDTO message) {
            var chatId = message.getChatId();
            ContextHolder.setNextCommand(chatId, new InputCorporateEmailCommand());
            return MessageToUser.builder()
                    .text("Введите корпоративную почту руководителя практики от компании")
                    .keyboardMarkup(getReturnToStartMarkup())
                    .needRewriting(true)
                    .build();
        }

        @Override
        public boolean isNextCallNeeded() {
            return false;
        }
    }
