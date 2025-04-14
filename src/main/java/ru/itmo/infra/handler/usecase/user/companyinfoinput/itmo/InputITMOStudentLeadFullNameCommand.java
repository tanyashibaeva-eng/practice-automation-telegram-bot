package ru.itmo.infra.handler.usecase.user.companyinfoinput.itmo;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.CompanyInfoUpdateArgs;
import ru.itmo.domain.dto.command.ITMOPracticeInfoUpdateArgs;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.company.AskingLeadPhoneNumberCommand;

public class InputITMOStudentLeadFullNameCommand implements Command {
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var leadName = message.getText().trim();
        if (!isValidFullName(leadName)) {
            ContextHolder.setNextCommand(chatId, new InputITMOStudentLeadFullNameCommand());
            return MessageToUser.builder()
                    .text("Введите ФИО корректно")
                    .build();
        }

        var dto = ContextHolder.getCommandData(message.getChatId());
        leadName = leadName.trim().replaceAll(" +", " ");
        if (dto instanceof ITMOPracticeInfoUpdateArgs) {
            var itmoArgs = (ITMOPracticeInfoUpdateArgs) dto;
            itmoArgs.setCompanyLeadFullName(leadName);
            ContextHolder.setCommandData(chatId, itmoArgs);
            ContextHolder.setNextCommand(chatId, new AskingITMOPracticeDepartmentCommand());
        } else {
            var companyArgs = (CompanyInfoUpdateArgs) dto;
            companyArgs.setCompanyLeadFullName(leadName);
            ContextHolder.setCommandData(chatId, companyArgs);
            ContextHolder.setNextCommand(chatId, new AskingLeadPhoneNumberCommand());
        }

        ContextHolder.setCommandData(chatId, dto);
        return MessageToUser.builder()
                .text("")
                .build();
    }

    private boolean isValidFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return false;
        }

        return fullName.matches("[a-zA-Zа-яА-ЯёЁ -]{2,}");
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "/who_itmo";
    }
}
