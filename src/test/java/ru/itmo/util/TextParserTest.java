package ru.itmo.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.BadRequestException;

public class TextParserTest {

    private final TextParser textParser = new TextParser();

    @Test
    public void testParseInt_ValidInput() throws BadRequestException {
        assertEquals(123, textParser.parseInt(" 123 "));
    }

    @Test
    public void testParseInt_InvalidInput() {
        var exception = assertThrows(BadRequestException.class, () -> {
            textParser.parseInt("abc");
        });
        assertEquals("должно быть числом", exception.getMessage());
    }

    @Test
    public void testParsePhone_ValidPhone() throws BadRequestException {
        assertEquals("+1234567890", textParser.parsePhone("+1234567890"));
        assertEquals("1234567890", textParser.parsePhone("1234567890"));
        assertEquals("+7 925 123 45 67", textParser.parsePhone("+7 925 123 45 67"));
        assertEquals("8 925 123 45 67", textParser.parsePhone("8 925 123 45 67"));
    }

    @Test
    public void testParsePhone_InvalidPhone() {
        var exception = assertThrows(BadRequestException.class, () -> {
            textParser.parsePhone("12345");
            assertEquals("+7 (925) 123 45 67", textParser.parsePhone("+7 (925) 123 45 67"));
        });
        assertEquals("неверный формат номера телефона", exception.getMessage());

        exception = assertThrows(BadRequestException.class, () -> {
            textParser.parsePhone("   ");
        });
        assertEquals("должно быть строкой, представляющей номер телефона.", exception.getMessage());
    }

    @Test
    public void testParseEmail_ValidEmail() throws BadRequestException {
        assertEquals("example@example.com", textParser.parseEmail("example@example.com"));
    }

    @Test
    public void testParseEmail_InvalidEmail() {
        var exception = assertThrows(BadRequestException.class, () -> {
            textParser.parseEmail("example@com");
        });
        assertEquals("неверный формат email", exception.getMessage());

        exception = assertThrows(BadRequestException.class, () -> {
            textParser.parseEmail("   ");
        });
        assertEquals("должно быть строкой, представляющей email.", exception.getMessage());
    }

    @Test
    public void testParseStatus_ValidStatus() throws BadRequestException {
        assertEquals(StudentStatus.APPLICATION_RETURNED, textParser.parseStatus("application_returned"));
        assertEquals(StudentStatus.REGISTERED, textParser.parseStatus("registered"));
    }

    @Test
    public void testParseStatus_InvalidStatus() {
        var exception = assertThrows(BadRequestException.class, () -> {
            textParser.parseStatus("unknown_status");
        });
        assertEquals("неверный статус", exception.getMessage());

        exception = assertThrows(BadRequestException.class, () -> {
            textParser.parseStatus("   ");
        });
        assertEquals("должно быть строкой, представляющей статус.", exception.getMessage());
    }
}
