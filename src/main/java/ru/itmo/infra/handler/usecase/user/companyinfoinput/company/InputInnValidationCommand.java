package ru.itmo.infra.handler.usecase.user.companyinfoinput.company;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.CompanyInfoUpdateArgs;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.itmo.AskingITMOPracticeLeadFullNameCommand;

public class InputInnValidationCommand implements Command {
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var  userText = message.getText().trim();
        var dto = (CompanyInfoUpdateArgs) ContextHolder.getCommandData(chatId);
        //валидация инн
        var innResponse = StudentService.validateInn(userText);
        if (innResponse.getErrorText() != null) {
            ContextHolder.setNextCommand(chatId, this);
            return MessageToUser.builder()
                    .text(innResponse.getErrorText())
                    .build();
        }
        // Если ИНН начинается не с "78", идем к запросу формата практики OK тут еще добавили если формат практики онлайн
        if (!innResponse.isSPB() && dto.getPracticeFormat() != PracticeFormat.ONLINE) {
            ContextHolder.setNextCommand(chatId, new AskingPracticeFormatCommand());
            return MessageToUser.builder()
                    .text("Для компаний не из Санкт-Петербурга формат прохождения практики может быть только дистанционным")
                    .build();
        }
        // Если ИНН корректен, проверяем договор с ИТМО OK
        if (!innResponse.isPresentInITMOAgreementFile()) {
            ContextHolder.setNextCommand(chatId, new AskingApproveNoContractCompanyCommand());
            return MessageToUser.builder()
                    .text("")
                    .build();
        }
        dto.setInn(innResponse.getInn()); //получили инн
        // Если компания не найдена, просим ввести название
        if (innResponse.getCompanyName() == null) {
            ContextHolder.setCommandData(chatId, dto); // сохранили инн
            ContextHolder.setNextCommand(chatId, new AskingCompanyNameCommand());
            return MessageToUser.builder()
                    .text("")
                    .build();
        }
        // сохранение и отправка
        dto.setCompanyName(innResponse.getCompanyName()); // сохранили название компании
        ContextHolder.setCommandData(chatId, dto);
        ContextHolder.setNextCommand(chatId, new AskingITMOPracticeLeadFullNameCommand());
        return MessageToUser.builder()
                .text("")
                .keyboardMarkup(getConfirmationKeyboard())
                .build();
    }
    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "/company_practice";
    }
}
