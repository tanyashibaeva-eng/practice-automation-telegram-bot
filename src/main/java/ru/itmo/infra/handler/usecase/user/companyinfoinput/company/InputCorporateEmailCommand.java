package ru.itmo.infra.handler.usecase.user.companyinfoinput.company;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.CompanyInfoUpdateArgs;
import ru.itmo.exception.BadRequestException;
import ru.itmo.infra.handler.usecase.user.UserCommand;
import ru.itmo.util.TextUtils;

    public class InputCorporateEmailCommand implements UserCommand {
        @SneakyThrows
        public MessageToUser execute(MessageDTO message) {
            var chatId = message.getChatId();
            var email = message.getText().trim();

            if (!isValidCorporateEmail(email)) {
                ContextHolder.setNextCommand(chatId, this);
                return MessageToUser.builder()
                        .text("Email имеет некорректный формат или не является корпоративным")
                        .keyboardMarkup(getReturnToStartMarkup())
                        .needRewriting(true)
                        .build();
            }

            var dto = (CompanyInfoUpdateArgs) ContextHolder.getCommandData(chatId);
            dto.setCompanyLeadEmail(email);
            ContextHolder.setCommandData(chatId, dto);
            ContextHolder.setNextCommand(chatId, new CompanyInfoSummaryCommand());
            return MessageToUser.builder()
                    .text("")
                    .build();
        }

        private static final String[] PERSONAL_EMAIL_DOMAINS = {
                "gmail.com", "yahoo.com", "yandex.ru", "mail.ru",
                "outlook.com", "hotmail.com", "rambler.ru", "icloud.com", "bk.ru", "vk.ru"
        };

        private boolean isValidCorporateEmail(String email) {
            if (email == null || email.trim().isEmpty()) {
                return false;
            }
            try {
                TextUtils.parseEmail(email);
            } catch (BadRequestException e) {
                return false;
            }
            String domain = extractDomain(email.toLowerCase());
            for (String personalDomain : PERSONAL_EMAIL_DOMAINS) {
                if (domain.equals(personalDomain) || domain.endsWith("." + personalDomain)) {
                    return false;
                }
            }
            return true;
        }

        private String extractDomain(String email) {
            int atIndex = email.indexOf('@');
            return atIndex == -1 ? "" : email.substring(atIndex + 1);
        }

        @Override
        public boolean isNextCallNeeded() {
            return true;
        }
    }
