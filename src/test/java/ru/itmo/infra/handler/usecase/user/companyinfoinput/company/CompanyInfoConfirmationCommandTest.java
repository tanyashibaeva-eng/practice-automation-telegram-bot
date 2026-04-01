package ru.itmo.infra.handler.usecase.user.companyinfoinput.company;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.domain.dto.command.CompanyInfoUpdateArgs;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.SubmitCompanyApprovalRequestCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.itmo.AskingITMOPracticeLeadFullNameCommand;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CompanyInfoConfirmationCommandTest {
    private static final long CHAT_ID = 42L;
    private static final String NO = "\u041d\u0435\u0442";
    private static final String YES = "\u0414\u0430";

    @AfterEach
    void tearDown() {
        ContextHolder.endCommand(CHAT_ID);
    }

    @Test
    void noShouldSkipKnownCompanyNameAndAddress() throws Exception {
        ContextHolder.setCommandData(CHAT_ID, CompanyInfoUpdateArgs.builder()
                .chatId(CHAT_ID)
                .practiceFormat(PracticeFormat.OFFLINE)
                .companyName("Тестовая компания")
                .companyAddress("Санкт-Петербург")
                .presentInITMOAgreementFile(true)
                .build());

        new CompanyInfoConfirmationCommand().execute(MessageDTO.builder()
                .chatId(CHAT_ID)
                .text(NO)
                .build());

        assertInstanceOf(AskingITMOPracticeLeadFullNameCommand.class, ContextHolder.getNextCommand(CHAT_ID));
    }

    @Test
    void yesShouldSubmitAdminRequestForCompanyWithoutAgreement() throws Exception {
        ContextHolder.setCommandData(CHAT_ID, CompanyInfoUpdateArgs.builder()
                .chatId(CHAT_ID)
                .practiceFormat(PracticeFormat.OFFLINE)
                .companyName("Тестовая компания")
                .companyAddress("Санкт-Петербург")
                .presentInITMOAgreementFile(false)
                .requiresSpbOfficeApproval(false)
                .build());

        new CompanyInfoConfirmationCommand().execute(MessageDTO.builder()
                .chatId(CHAT_ID)
                .text(YES)
                .build());

        assertInstanceOf(SubmitCompanyApprovalRequestCommand.class, ContextHolder.getNextCommand(CHAT_ID));
    }
}
