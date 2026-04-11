package ru.itmo.infra.handler.usecase.admin.forceupdate;

import org.junit.jupiter.api.Test;
import ru.itmo.exception.BadRequestException;

import static org.junit.jupiter.api.Assertions.*;

public class ForceUpdateCommandParserTest {

    @Test
    public void testParse_SingleField() throws BadRequestException {
        String command = "/forceupdate 123456 \"Тестовый поток\" status=\"PRACTICE_APPROVED\"";
        ForceUpdateCommandParser parser = ForceUpdateCommandParser.parse(command);
        
        assertEquals(123456, parser.getStudentIsu());
        assertEquals("Тестовый поток", parser.getEduStreamName());
        assertFalse(parser.isDryRun());
        assertFalse(parser.isShowFields());
        assertTrue(parser.hasFieldsToUpdate());
        assertEquals(1, parser.getFieldValues().size());
        assertTrue(parser.getFieldValues().containsKey(ForceUpdateField.STATUS));
        assertEquals("PRACTICE_APPROVED", parser.getFieldValues().get(ForceUpdateField.STATUS));
    }

    @Test
    public void testParse_MultipleFields() throws BadRequestException {
        String command = "/forceupdate 999888 \"Весна 2026\" status=\"PRACTICE_APPROVED\" place=\"ITMO_UNIVERSITY\" format=\"OFFLINE\"";
        ForceUpdateCommandParser parser = ForceUpdateCommandParser.parse(command);
        
        assertEquals(999888, parser.getStudentIsu());
        assertEquals("Весна 2026", parser.getEduStreamName());
        assertTrue(parser.hasFieldsToUpdate());
        assertEquals(3, parser.getFieldValues().size());
        assertEquals("PRACTICE_APPROVED", parser.getFieldValues().get(ForceUpdateField.STATUS));
        assertEquals("ITMO_UNIVERSITY", parser.getFieldValues().get(ForceUpdateField.PRACTICE_PLACE));
        assertEquals("OFFLINE", parser.getFieldValues().get(ForceUpdateField.PRACTICE_FORMAT));
    }

    @Test
    public void testParse_DryRunMode() throws BadRequestException {
        String command = "/forceupdate --dry-run 123456 \"Тестовый поток\" status=\"PRACTICE_APPROVED\"";
        ForceUpdateCommandParser parser = ForceUpdateCommandParser.parse(command);
        
        assertTrue(parser.isDryRun());
        assertEquals(123456, parser.getStudentIsu());
        assertEquals("Тестовый поток", parser.getEduStreamName());
    }

    @Test
    public void testParse_HelpCommand() throws BadRequestException {
        String command = "/forceupdate --help";
        ForceUpdateCommandParser parser = ForceUpdateCommandParser.parse(command);
        
        assertTrue(parser.isShowFields());
        assertFalse(parser.isDryRun());
        assertEquals(0, parser.getStudentIsu());
        assertNull(parser.getEduStreamName());
        assertFalse(parser.hasFieldsToUpdate());
    }

    @Test
    public void testParse_WithQuotedName() throws BadRequestException {
        String command = "/forceupdate 123456 \"Поток с пробелами и 2026\" status=\"REGISTERED\"";
        ForceUpdateCommandParser parser = ForceUpdateCommandParser.parse(command);
        
        assertEquals("Поток с пробелами и 2026", parser.getEduStreamName());
    }

    @Test
    public void testParse_EmptyFields() throws BadRequestException {
        String command = "/forceupdate 123456 \"Поток\"";
        ForceUpdateCommandParser parser = ForceUpdateCommandParser.parse(command);
        
        assertFalse(parser.hasFieldsToUpdate());
        assertTrue(parser.getFieldValues().isEmpty());
    }

    @Test
    public void testParse_RussianAliases() throws BadRequestException {
        String command = "/forceupdate 123456 \"Поток\" статус=\"REGISTERED\"";
        ForceUpdateCommandParser parser = ForceUpdateCommandParser.parse(command);
        
        assertEquals("REGISTERED", parser.getFieldValues().get(ForceUpdateField.STATUS));
    }

    @Test
    public void testParse_ShortAliases() throws BadRequestException {
        String command = "/forceupdate 123456 \"Поток\" mp=\"OTHER_COMPANY\"";
        ForceUpdateCommandParser parser = ForceUpdateCommandParser.parse(command);
        
        assertEquals("OTHER_COMPANY", parser.getFieldValues().get(ForceUpdateField.PRACTICE_PLACE));
    }

    @Test
    public void testParse_CompanyFields() throws BadRequestException {
        String command = "/forceupdate 123456 \"Поток\" inn=\"7801234567\" company=\"ООО Ромашка\" lead=\"Иванов И.И.\" phone=\"+79000000000\" email=\"boss@company.com\" title=\"Директор\"";
        ForceUpdateCommandParser parser = ForceUpdateCommandParser.parse(command);
        
        assertEquals("7801234567", parser.getFieldValues().get(ForceUpdateField.COMPANY_INN));
        assertEquals("ООО Ромашка", parser.getFieldValues().get(ForceUpdateField.COMPANY_NAME));
        assertEquals("Иванов И.И.", parser.getFieldValues().get(ForceUpdateField.LEAD_FULL_NAME));
        assertEquals("+79000000000", parser.getFieldValues().get(ForceUpdateField.LEAD_PHONE));
        assertEquals("boss@company.com", parser.getFieldValues().get(ForceUpdateField.LEAD_EMAIL));
        assertEquals("Директор", parser.getFieldValues().get(ForceUpdateField.LEAD_JOB_TITLE));
    }

    @Test
    public void testParse_InvalidFormat_NoIsu() {
        String command = "/forceupdate \"Поток\" status=\"REGISTERED\"";
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ForceUpdateCommandParser.parse(command);
        });
        assertTrue(exception.getMessage().contains("Неверный формат команды"));
    }

    @Test
    public void testParse_InvalidFormat_NoStreamName() {
        String command = "/forceupdate 123456 status=\"REGISTERED\"";
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ForceUpdateCommandParser.parse(command);
        });
        assertTrue(exception.getMessage().contains("Неверный формат команды"));
    }

    @Test
    public void testParse_InvalidFormat_EmptyCommand() {
        String command = "/forceupdate";
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ForceUpdateCommandParser.parse(command);
        });
        assertTrue(exception.getMessage().contains("Неверный формат команды"));
    }

    @Test
    public void testParse_InvalidField() {
        String command = "/forceupdate 123456 \"Поток\" unknownfield=\"value\"";
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ForceUpdateCommandParser.parse(command);
        });
        assertTrue(exception.getMessage().contains("Неизвестное поле"));
    }

    @Test
    public void testParse_DuplicateField() {
        String command = "/forceupdate 123456 \"Поток\" status=\"REGISTERED\" status=\"PRACTICE_APPROVED\"";
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ForceUpdateCommandParser.parse(command);
        });
        assertTrue(exception.getMessage().contains("указано несколько раз"));
    }

    @Test
    public void testParse_InvalidIsu() {
        String command = "/forceupdate abc \"Поток\" status=\"REGISTERED\"";
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ForceUpdateCommandParser.parse(command);
        });
        assertTrue(exception.getMessage().contains("Неверный тип isu") || exception.getMessage().contains("Неверный формат команды"));
    }

    @Test
    public void testParse_InvalidFieldValue() {
        String command = "/forceupdate 123456 \"Поток\" status=\"INVALID_STATUS\"";
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ForceUpdateCommandParser.parse(command);
        });
        assertTrue(exception.getMessage().contains("Ошибка валидации поля"));
    }

    @Test
    public void testParse_InvalidInn() {
        String command = "/forceupdate 123456 \"Поток\" inn=\"123\"";
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ForceUpdateCommandParser.parse(command);
        });
        assertTrue(exception.getMessage().contains("Ошибка валидации поля"));
    }

    @Test
    public void testParse_InvalidPhone() {
        String command = "/forceupdate 123456 \"Поток\" phone=\"invalid\"";
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ForceUpdateCommandParser.parse(command);
        });
        assertTrue(exception.getMessage().contains("Ошибка валидации поля"));
    }

    @Test
    public void testParse_InvalidEmail() {
        String command = "/forceupdate 123456 \"Поток\" email=\"not-an-email\"";
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ForceUpdateCommandParser.parse(command);
        });
        assertTrue(exception.getMessage().contains("Ошибка валидации поля"));
    }

    @Test
    public void testParse_NullValue() {
        String command = "/forceupdate 123456 \"Поток\" status=\"\"";
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ForceUpdateCommandParser.parse(command);
        });
        assertTrue(exception.getMessage().contains("Ошибка валидации поля"));
    }
}