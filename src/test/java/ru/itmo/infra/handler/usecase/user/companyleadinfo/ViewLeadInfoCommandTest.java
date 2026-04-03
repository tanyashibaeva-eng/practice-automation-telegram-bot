package ru.itmo.infra.handler.usecase.user.companyleadinfo;

import org.junit.jupiter.api.Test;
import ru.itmo.domain.type.StudentStatus;

import static org.junit.jupiter.api.Assertions.*;

class ViewLeadInfoCommandTest {

    private final ViewLeadInfoCommand command = new ViewLeadInfoCommand();

    @Test
    void nameShouldBeViewLeadInfo() {
        assertEquals("/view_lead_info", command.getName());
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
    void shouldBeAvailableForApplicationWaitingSubmission() {
        assertTrue(command.isAvailableForStatus(StudentStatus.APPLICATION_WAITING_SUBMISSION));
    }

    @Test
    void shouldBeAvailableForApplicationSigned() {
        assertTrue(command.isAvailableForStatus(StudentStatus.APPLICATION_SIGNED));
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
}
