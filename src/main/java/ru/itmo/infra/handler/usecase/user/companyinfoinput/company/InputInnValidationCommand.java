package ru.itmo.infra.handler.usecase.user.companyinfoinput.company;

import lombok.extern.java.Log;
import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.CompanyInfoUpdateArgs;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.infra.handler.usecase.user.UserCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.itmo.AskingITMOPracticeLeadFullNameCommand;

@Log
public class InputInnValidationCommand implements UserCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var userText = message.getText().trim();
        var dto = (CompanyInfoUpdateArgs) ContextHolder.getCommandData(chatId);

        var innResponse = StudentService.validateInn(userText);
        if (innResponse.getErrorText() != null) {
            ContextHolder.setNextCommand(chatId, this);
            return MessageToUser.builder()
                    .text(innResponse.getErrorText())
                    .keyboardMarkup(getReturnToStartMarkup())
                    .needRewriting(true)
                    .build();
        }

        dto.setInn(innResponse.getInn());
        dto.setCompanyName(innResponse.getCompanyName());
        dto.setPresentInITMOAgreementFile(innResponse.isPresentInITMOAgreementFile());
        dto.setRequiresSpbOfficeApproval(false);
        ContextHolder.setCommandData(chatId, dto);

        boolean needsSpbOfficeApproval = dto.getPracticeFormat() != PracticeFormat.ONLINE && !innResponse.isSPB();
        log.info("INN " + innResponse.getInn()
                + ": practiceFormat=" + dto.getPracticeFormat()
                + ", isSPB=" + innResponse.isSPB()
                + ", requiresSpbOfficeApproval=" + needsSpbOfficeApproval);
        if (needsSpbOfficeApproval) {
            dto.setRequiresSpbOfficeApproval(true);
            ContextHolder.setCommandData(chatId, dto);
            ContextHolder.setNextCommand(chatId, innResponse.getCompanyName() == null
                    ? new AskingCompanyNameCommand()
                    : new AskingCompanyAddressCommand());
            var text = innResponse.isNonSpbCompany() && innResponse.getCompanyName() != null
                    ? "Компания не зарегистрирована в СПб. Не удалось определить адрес офиса в СПб."
                    : (
                            innResponse.getCompanyName() == null
                            ? "Не удалось получить данные о компании через egrul.nalog.ru."
                            : ""
                    );
            return MessageToUser.builder()
                    .text(text)
                    .build();
        }

        if (innResponse.getCompanyName() == null) {
            ContextHolder.setCommandData(chatId, dto);
            ContextHolder.setNextCommand(chatId, new AskingCompanyNameCommand());
            return MessageToUser.builder()
                    .text("Не удалось получить данные о компании.")
                    .build();
        }

        var text = innResponse.isNonSpbCompany()
                ? "Компания не зарегистрирована в СПб. Офис в СПб был найден."
                : "Подтверждена регистрация компании в СПб";

        text = dto.getPracticeFormat() != PracticeFormat.ONLINE
                ? text
                : "";

        if (!innResponse.isPresentInITMOAgreementFile()) {
            ContextHolder.setNextCommand(chatId, new AskingApproveNoContractCompanyCommand());
            return MessageToUser.builder()
                    .text(text)
                    .build();
        }

        ContextHolder.setNextCommand(chatId, new AskingITMOPracticeLeadFullNameCommand());
        return MessageToUser.builder()
                .text(text)
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }
}
