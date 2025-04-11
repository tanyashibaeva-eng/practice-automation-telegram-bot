package ru.itmo.infra.handler.usecase.companyinfoinput.itmo;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.CompanyInfoUpdateArgs;
import ru.itmo.domain.dto.command.ITMOPracticeInfoUpdateArgs;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.handler.usecase.companyinfoinput.StudentInputConfirmationCommand;

public class InputITMOStudentDepartmentCommand implements Command {
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var locationName = message.getText().trim();
        if (!isValidLocation(locationName)) {
            ContextHolder.setNextCommand(chatId, new InputITMOStudentDepartmentCommand());
            return MessageToUser.builder()
                    .text("Введите подразделение корректно")
                    .build();
        }

        var dto = ContextHolder.getCommandData(message.getChatId());
        locationName = locationName.trim().replaceAll(" +", " ");
        if (dto instanceof ITMOPracticeInfoUpdateArgs) {
            var itmoArgs = (ITMOPracticeInfoUpdateArgs) dto;
            itmoArgs.setCompanyName(locationName);
            ContextHolder.setCommandData(chatId, itmoArgs);
            var leadName = itmoArgs.getCompanyLeadFullName();

            ContextHolder.setNextCommand(chatId, new StudentInputConfirmationCommand());
            return MessageToUser.builder()
                    .text("Вы будете проходить практику в ИТМО в подразделении %s, у %s ?".formatted(locationName, leadName))
                    .keyboardMarkup(getInlineKeyboard())
                    .build();

        }

        var companyArgs = (CompanyInfoUpdateArgs) dto;
        companyArgs.setCompanyName(locationName);
        ContextHolder.setCommandData(chatId, companyArgs);
        var leadName = companyArgs.getCompanyLeadFullName();

        // TODO:
        return null;
//        ContextHolder.setNextCommand(chatId, new StudentInputConfirmationCommand());
//        return MessageToUser.builder()
//                .text("Вы будете проходить практику в ИТМО в подразделении %s, у %s ?".formatted(locationName, leadName))
//                .keyboardMarkup(getInlineKeyboard())
//                .build();
    }

    private boolean isValidLocation(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return false;
        }

        return fullName.matches("[a-zA-Zа-яА-ЯёЁ -]{2,}");
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/who_itmo";
    }
}
