package ru.itmo.util;

import org.junit.jupiter.api.Test;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.BadRequestException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TextParserTest {

    private final TextParser textParser = new TextParser();

    @Test
    public void testParseIsu_ValidInput() throws BadRequestException {
        assertEquals(123, TextParser.parseIsu(" 123 "));
    }

    @Test
    public void testParseIsu_InvalidInput() {
        var exception = assertThrows(BadRequestException.class, () -> {
            TextParser.parseIsu("abc");
        });
        assertEquals("Номер ИСУ должен быть числом", exception.getMessage());
    }

    @Test
    public void testParsePhone_ValidPhone() throws BadRequestException {
        assertEquals("+1234567890", TextParser.parsePhone("+1234567890"));
        assertEquals("1234567890", TextParser.parsePhone("1234567890"));
        assertEquals("+7 925 123 45 67", TextParser.parsePhone("+7 925 123 45 67"));
        assertEquals("8 925 123 45 67", TextParser.parsePhone("8 925 123 45 67"));
    }

    @Test
    public void testParsePhone_InvalidPhone() {
        var exception = assertThrows(BadRequestException.class, () -> {
            TextParser.parsePhone("12345");
        });
        assertEquals("неверный формат номера телефона", exception.getMessage());

        exception = assertThrows(BadRequestException.class, () -> {
            TextParser.parsePhone("+7 (925) 123 45 67");
        });
        assertEquals("неверный формат номера телефона", exception.getMessage());

        exception = assertThrows(BadRequestException.class, () -> {
            TextParser.parsePhone("   ");
        });
        assertEquals("должно быть строкой, представляющей номер телефона.", exception.getMessage());
    }

    @Test
    public void testParseEmail_ValidEmail() throws BadRequestException {
        assertEquals("example@example.com", TextParser.parseEmail("example@example.com"));
    }

    @Test
    public void testParseEmail_InvalidEmail() {
        var exception = assertThrows(BadRequestException.class, () -> {
            TextParser.parseEmail("example@com");
        });
        assertEquals("неверный формат email", exception.getMessage());

        exception = assertThrows(BadRequestException.class, () -> {
            TextParser.parseEmail("   ");
        });
        assertEquals("должно быть строкой, представляющей email.", exception.getMessage());
    }

    @Test
    public void testParseStatus_ValidStatus() throws BadRequestException {
        assertEquals(StudentStatus.APPLICATION_RETURNED, TextParser.parseStatus("Заявка возвращена на доработку"));
        assertEquals(StudentStatus.REGISTERED, TextParser.parseStatus("Зарегистрирован"));
    }

    @Test
    public void testParseStatus_InvalidStatus() {
        var exception = assertThrows(BadRequestException.class, () -> {
            TextParser.parseStatus("unknown_status");
        });
        assertEquals("неверный статус", exception.getMessage());

        exception = assertThrows(BadRequestException.class, () -> {
            TextParser.parseStatus("   ");
        });
        assertEquals("должно быть строкой, представляющей статус.", exception.getMessage());
    }
}
