package ru.itmo.infra.handler.usecase.user.companyleadinfo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LeadInfoFieldTest {

    @Test
    void fromStringShouldReturnCorrectField() {
        assertEquals(LeadInfoField.FULLNAME, LeadInfoField.fromString("FULLNAME"));
        assertEquals(LeadInfoField.LASTNAME, LeadInfoField.fromString("lastname"));
        assertEquals(LeadInfoField.PHONE, LeadInfoField.fromString("Phone"));
    }

    @Test
    void fromStringShouldReturnNullForUnknown() {
        assertNull(LeadInfoField.fromString("UNKNOWN"));
        assertNull(LeadInfoField.fromString(""));
    }

    @Test
    void isNamePartShouldReturnTrueForNameFields() {
        assertTrue(LeadInfoField.LASTNAME.isNamePart());
        assertTrue(LeadInfoField.FIRSTNAME.isNamePart());
        assertTrue(LeadInfoField.PATRONYMIC.isNamePart());
    }

    @Test
    void isNamePartShouldReturnFalseForOtherFields() {
        assertFalse(LeadInfoField.FULLNAME.isNamePart());
        assertFalse(LeadInfoField.PHONE.isNamePart());
        assertFalse(LeadInfoField.EMAIL.isNamePart());
        assertFalse(LeadInfoField.JOB_TITLE.isNamePart());
    }

    @Test
    void columnShouldBeCorrectForEachField() {
        assertEquals("company_lead_fullname", LeadInfoField.FULLNAME.getColumn());
        assertEquals("company_lead_fullname", LeadInfoField.LASTNAME.getColumn());
        assertEquals("company_lead_fullname", LeadInfoField.FIRSTNAME.getColumn());
        assertEquals("company_lead_fullname", LeadInfoField.PATRONYMIC.getColumn());
        assertEquals("company_lead_phone", LeadInfoField.PHONE.getColumn());
        assertEquals("company_lead_email", LeadInfoField.EMAIL.getColumn());
        assertEquals("company_lead_job_title", LeadInfoField.JOB_TITLE.getColumn());
    }

    @Test
    void allFieldsShouldHaveDisplayName() {
        for (var field : LeadInfoField.values()) {
            assertNotNull(field.getDisplayName());
            assertFalse(field.getDisplayName().isBlank());
        }
    }
}
