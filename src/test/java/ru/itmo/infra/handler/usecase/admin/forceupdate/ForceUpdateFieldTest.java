package ru.itmo.infra.handler.usecase.admin.forceupdate;

import org.junit.jupiter.api.Test;
import ru.itmo.exception.BadRequestException;

import static org.junit.jupiter.api.Assertions.*;

public class ForceUpdateFieldTest {

    @Test
    public void testFindByKey_ValidEnglishKeys() {
        assertEquals(ForceUpdateField.STATUS, ForceUpdateField.findByKey("status"));
        assertEquals(ForceUpdateField.PRACTICE_PLACE, ForceUpdateField.findByKey("place"));
        assertEquals(ForceUpdateField.PRACTICE_FORMAT, ForceUpdateField.findByKey("format"));
        assertEquals(ForceUpdateField.COMPANY_INN, ForceUpdateField.findByKey("inn"));
        assertEquals(ForceUpdateField.COMPANY_NAME, ForceUpdateField.findByKey("company"));
        assertEquals(ForceUpdateField.LEAD_FULL_NAME, ForceUpdateField.findByKey("lead"));
        assertEquals(ForceUpdateField.LEAD_PHONE, ForceUpdateField.findByKey("phone"));
        assertEquals(ForceUpdateField.LEAD_EMAIL, ForceUpdateField.findByKey("email"));
        assertEquals(ForceUpdateField.LEAD_JOB_TITLE, ForceUpdateField.findByKey("title"));
    }

    @Test
    public void testFindByKey_ValidRussianKeys() {
        assertEquals(ForceUpdateField.STATUS, ForceUpdateField.findByKey("статус"));
        assertEquals(ForceUpdateField.PRACTICE_PLACE, ForceUpdateField.findByKey("место"));
        assertEquals(ForceUpdateField.PRACTICE_FORMAT, ForceUpdateField.findByKey("формат"));
        assertEquals(ForceUpdateField.COMPANY_INN, ForceUpdateField.findByKey("инн"));
        assertEquals(ForceUpdateField.COMPANY_NAME, ForceUpdateField.findByKey("компания"));
        assertEquals(ForceUpdateField.LEAD_FULL_NAME, ForceUpdateField.findByKey("руководитель"));
        assertEquals(ForceUpdateField.LEAD_PHONE, ForceUpdateField.findByKey("телефон"));
        assertEquals(ForceUpdateField.LEAD_EMAIL, ForceUpdateField.findByKey("почта"));
        assertEquals(ForceUpdateField.LEAD_JOB_TITLE, ForceUpdateField.findByKey("должность"));
    }

    @Test
    public void testFindByKey_ShortAliases() {
        assertEquals(ForceUpdateField.PRACTICE_PLACE, ForceUpdateField.findByKey("mp"));
        assertEquals(ForceUpdateField.PRACTICE_FORMAT, ForceUpdateField.findByKey("fp"));
        assertEquals(ForceUpdateField.COMPANY_INN, ForceUpdateField.findByKey("ic"));
        assertEquals(ForceUpdateField.COMPANY_NAME, ForceUpdateField.findByKey("kc"));
        assertEquals(ForceUpdateField.LEAD_FULL_NAME, ForceUpdateField.findByKey("fr"));
        assertEquals(ForceUpdateField.LEAD_PHONE, ForceUpdateField.findByKey("tp"));
        assertEquals(ForceUpdateField.LEAD_EMAIL, ForceUpdateField.findByKey("pe"));
        assertEquals(ForceUpdateField.LEAD_JOB_TITLE, ForceUpdateField.findByKey("dr"));
    }

    @Test
    public void testFindByKey_CaseInsensitive() {
        assertEquals(ForceUpdateField.STATUS, ForceUpdateField.findByKey("STATUS"));
        assertEquals(ForceUpdateField.STATUS, ForceUpdateField.findByKey("Status"));
        assertEquals(ForceUpdateField.COMPANY_INN, ForceUpdateField.findByKey("INN"));
    }

    @Test
    public void testFindByKey_NullKey() {
        assertNull(ForceUpdateField.findByKey(null));
    }

    @Test
    public void testFindByKey_InvalidKey() {
        assertNull(ForceUpdateField.findByKey("unknown"));
        assertNull(ForceUpdateField.findByKey("invalid_field"));
        assertNull(ForceUpdateField.findByKey(""));
    }

    @Test
    public void testIsValidKey_ValidKeys() {
        assertTrue(ForceUpdateField.isValidKey("status"));
        assertTrue(ForceUpdateField.isValidKey("place"));
        assertTrue(ForceUpdateField.isValidKey("статус"));
    }

    @Test
    public void testIsValidKey_InvalidKeys() {
        assertFalse(ForceUpdateField.isValidKey("unknown"));
        assertFalse(ForceUpdateField.isValidKey(""));
    }

    @Test
    public void testValidateStatus_ValidValues() {
        assertDoesNotThrow(() -> ForceUpdateField.STATUS.getValidator().validate("REGISTERED"));
        assertDoesNotThrow(() -> ForceUpdateField.STATUS.getValidator().validate("PRACTICE_APPROVED"));
        assertDoesNotThrow(() -> ForceUpdateField.STATUS.getValidator().validate("APPLICATION_SIGNED"));
    }

    @Test
    public void testValidateStatus_InvalidValue() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ForceUpdateField.STATUS.getValidator().validate("INVALID_STATUS");
        });
        assertNotNull(exception.getMessage());
    }

    @Test
    public void testValidatePracticePlace_ValidValues() {
        assertDoesNotThrow(() -> ForceUpdateField.PRACTICE_PLACE.getValidator().validate("OTHER_COMPANY"));
        assertDoesNotThrow(() -> ForceUpdateField.PRACTICE_PLACE.getValidator().validate("ITMO_UNIVERSITY"));
        assertDoesNotThrow(() -> ForceUpdateField.PRACTICE_PLACE.getValidator().validate("ITMO_MARKINA"));
    }

    @Test
    public void testValidatePracticePlace_InvalidValue() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ForceUpdateField.PRACTICE_PLACE.getValidator().validate("INVALID_PLACE");
        });
        assertNotNull(exception.getMessage());
    }

    @Test
    public void testValidatePracticeFormat_ValidValues() {
        assertDoesNotThrow(() -> ForceUpdateField.PRACTICE_FORMAT.getValidator().validate("OFFLINE"));
        assertDoesNotThrow(() -> ForceUpdateField.PRACTICE_FORMAT.getValidator().validate("ONLINE"));
    }

    @Test
    public void testValidatePracticeFormat_InvalidValue() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ForceUpdateField.PRACTICE_FORMAT.getValidator().validate("INVALID_FORMAT");
        });
        assertNotNull(exception.getMessage());
    }

    @Test
    public void testValidateInn_ValidValues() {
        assertDoesNotThrow(() -> ForceUpdateField.COMPANY_INN.getValidator().validate("7801234567"));
        assertDoesNotThrow(() -> ForceUpdateField.COMPANY_INN.getValidator().validate("1234567890"));
    }

    @Test
    public void testValidateInn_InvalidTooShort() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ForceUpdateField.COMPANY_INN.getValidator().validate("123");
        });
        assertTrue(exception.getMessage().contains("10 цифр"));
    }

    @Test
    public void testValidateInn_InvalidTooLong() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ForceUpdateField.COMPANY_INN.getValidator().validate("123456789012");
        });
        assertTrue(exception.getMessage().contains("10 цифр"));
    }

    @Test
    public void testValidateInn_InvalidWithLetters() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ForceUpdateField.COMPANY_INN.getValidator().validate("123456789a");
        });
        assertTrue(exception.getMessage().contains("только цифры") || exception.getMessage().contains("10 цифр"));
    }

    @Test
    public void testValidateInn_EmptyValue() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ForceUpdateField.COMPANY_INN.getValidator().validate("");
        });
        assertTrue(exception.getMessage().contains("не может быть пустым"));
    }

    @Test
    public void testValidateCompanyName_ValidValue() {
        assertDoesNotThrow(() -> ForceUpdateField.COMPANY_NAME.getValidator().validate("ООО Ромашка"));
        assertDoesNotThrow(() -> ForceUpdateField.COMPANY_NAME.getValidator().validate("АО Тест"));
    }

    @Test
    public void testValidateCompanyName_EmptyValue() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ForceUpdateField.COMPANY_NAME.getValidator().validate("");
        });
        assertTrue(exception.getMessage().contains("не может быть пустым"));
    }

    @Test
    public void testValidateLeadFullName_ValidValue() {
        assertDoesNotThrow(() -> ForceUpdateField.LEAD_FULL_NAME.getValidator().validate("Иванов Иван Иванович"));
    }

    @Test
    public void testValidateLeadFullName_EmptyValue() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ForceUpdateField.LEAD_FULL_NAME.getValidator().validate("");
        });
        assertTrue(exception.getMessage().contains("не может быть пустым"));
    }

    @Test
    public void testValidatePhone_ValidFormats() {
        assertDoesNotThrow(() -> ForceUpdateField.LEAD_PHONE.getValidator().validate("+79000000000"));
        assertDoesNotThrow(() -> ForceUpdateField.LEAD_PHONE.getValidator().validate("89000000000"));
        assertDoesNotThrow(() -> ForceUpdateField.LEAD_PHONE.getValidator().validate("9000000000"));
        assertDoesNotThrow(() -> ForceUpdateField.LEAD_PHONE.getValidator().validate("+7 (900) 123 45 67"));
        assertDoesNotThrow(() -> ForceUpdateField.LEAD_PHONE.getValidator().validate("8-900-123-45-67"));
    }

    @Test
    public void testValidatePhone_InvalidValue() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ForceUpdateField.LEAD_PHONE.getValidator().validate("invalid");
        });
        assertTrue(exception.getMessage().contains("Неверный формат телефона"));
    }

    @Test
    public void testValidatePhone_EmptyValue() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ForceUpdateField.LEAD_PHONE.getValidator().validate("");
        });
        assertTrue(exception.getMessage().contains("не может быть пустым"));
    }

    @Test
    public void testValidateEmail_ValidValues() {
        assertDoesNotThrow(() -> ForceUpdateField.LEAD_EMAIL.getValidator().validate("boss@company.com"));
        assertDoesNotThrow(() -> ForceUpdateField.LEAD_EMAIL.getValidator().validate("test@mail.ru"));
    }

    @Test
    public void testValidateEmail_InvalidNoAt() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ForceUpdateField.LEAD_EMAIL.getValidator().validate("invalid-email.com");
        });
        assertTrue(exception.getMessage().contains("Неверный формат email"));
    }

    @Test
    public void testValidateEmail_InvalidNoDomain() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ForceUpdateField.LEAD_EMAIL.getValidator().validate("test@");
        });
        assertTrue(exception.getMessage().contains("Неверный формат email"));
    }

    @Test
    public void testValidateEmail_EmptyValue() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ForceUpdateField.LEAD_EMAIL.getValidator().validate("");
        });
        assertTrue(exception.getMessage().contains("не может быть пустым"));
    }

    @Test
    public void testValidateLeadJobTitle_ValidValue() {
        assertDoesNotThrow(() -> ForceUpdateField.LEAD_JOB_TITLE.getValidator().validate("Главный инженер"));
    }

    @Test
    public void testValidateLeadJobTitle_EmptyValue() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ForceUpdateField.LEAD_JOB_TITLE.getValidator().validate("");
        });
        assertTrue(exception.getMessage().contains("не может быть пустой"));
    }

    @Test
    public void testGetAvailableFieldsList_NotEmpty() {
        String fieldsList = ForceUpdateField.getAvailableFieldsList();
        assertNotNull(fieldsList);
        assertFalse(fieldsList.isEmpty());
        assertTrue(fieldsList.contains("статус"));
        assertTrue(fieldsList.contains("место"));
    }

    @Test
    public void testGetAllAliases_ContainsAllAliases() {
        var aliases = ForceUpdateField.getAllAliases();
        assertTrue(aliases.contains("status"));
        assertTrue(aliases.contains("статус"));
        assertTrue(aliases.contains("place"));
        assertTrue(aliases.contains("mp"));
        assertTrue(aliases.contains("inn"));
        assertTrue(aliases.contains("инн"));
    }

    @Test
    public void testGetAliasesList_NotEmpty() {
        String aliasesList = ForceUpdateField.getAliasesList();
        assertNotNull(aliasesList);
        assertFalse(aliasesList.isEmpty());
        assertTrue(aliasesList.contains("статус ->"));
    }
}