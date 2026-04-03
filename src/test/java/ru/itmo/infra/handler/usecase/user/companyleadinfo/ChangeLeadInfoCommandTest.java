package ru.itmo.infra.handler.usecase.user.companyleadinfo;

import org.junit.jupiter.api.Test;
import ru.itmo.bot.MessageDTO;
import ru.itmo.domain.type.StudentStatus;

import static org.junit.jupiter.api.Assertions.*;

class ChangeLeadInfoCommandTest {

    private final ChangeLeadInfoCommand command = new ChangeLeadInfoCommand();

    @Test
    void executeShouldReturnMenuText() {
        var message = MessageDTO.builder().chatId(1L).text("/change_lead_info").build();
        var result = command.execute(message);
        assertTrue(result.getText().contains("руководителя практики от компании"));
        assertNotNull(result.getKeyboardMarkup());
    }

    @Test
    void nameShouldBeChangeLeadInfo() {
        assertEquals("/change_lead_info", command.getName());
    }

    @Test
    void shouldNotNeedNextCall() {
        assertFalse(command.isNextCallNeeded());
    }

    @Test
    void shouldNotBeAvailableForNotRegistered() {
        assertFalse(command.isAvailableForStatus(StudentStatus.NOT_REGISTERED));
    }

    @Test
    void shouldNotBeAvailableForRegistered() {
        assertFalse(command.isAvailableForStatus(StudentStatus.REGISTERED));
    }

    @Test
    void shouldNotBeAvailableForItmoMarkina() {
        assertFalse(command.isAvailableForStatus(StudentStatus.PRACTICE_IN_ITMO_MARKINA));
    }

    @Test
    void shouldNotBeAvailableForPracticeApproved() {
        assertFalse(command.isAvailableForStatus(StudentStatus.PRACTICE_APPROVED));
    }

    @Test
    void shouldBeAvailableForCompanyInfoWaitingApproval() {
        assertTrue(command.isAvailableForStatus(StudentStatus.COMPANY_INFO_WAITING_APPROVAL));
    }

    @Test
    void shouldBeAvailableForCompanyInfoReturned() {
        assertTrue(command.isAvailableForStatus(StudentStatus.COMPANY_INFO_RETURNED));
    }

    @Test
    void shouldBeAvailableForApplicationWaitingSubmission() {
        assertTrue(command.isAvailableForStatus(StudentStatus.APPLICATION_WAITING_SUBMISSION));
    }

    @Test
    void shouldBeAvailableForApplicationWaitingApproval() {
        assertTrue(command.isAvailableForStatus(StudentStatus.APPLICATION_WAITING_APPROVAL));
    }

    @Test
    void shouldBeAvailableForApplicationReturned() {
        assertTrue(command.isAvailableForStatus(StudentStatus.APPLICATION_RETURNED));
    }

    @Test
    void shouldNotBeAvailableForNullStatus() {
        assertFalse(command.isAvailableForStatus(null));
    }

    @Test
    void shouldNotBeAdminCommand() {
        assertFalse(command.isAdminCommand());
    }

    @Test
    void descriptionShouldNotBeEmpty() {
        assertFalse(command.getDescription().isEmpty());
    }

    @Test
    void displayNameShouldNotBeEmpty() {
        assertFalse(command.getDisplayName().isEmpty());
    }
}
