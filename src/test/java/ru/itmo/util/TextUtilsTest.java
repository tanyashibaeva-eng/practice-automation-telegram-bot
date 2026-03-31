package ru.itmo.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.BadRequestException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TextUtilsTest {

    private final TextUtils textUtils = new TextUtils();

    @Test
    public void testParseIsu_ValidInput() throws BadRequestException {
        assertEquals(123, TextUtils.parseIsu(" 123 "));
    }

    @Test
    public void testParseIsu_InvalidInput() {
        var exception = assertThrows(BadRequestException.class, () -> {
            TextUtils.parseIsu("abc");
        });
        assertEquals("Номер ИСУ должен быть числом", exception.getMessage());
    }

    @Test
    public void testParsePhone_ValidPhone() throws BadRequestException {
        assertEquals("+7 925 123 45 67", TextUtils.parsePhone("+7 925 123 45 67"));
        assertEquals("8 925 123 45 67", TextUtils.parsePhone("8 925 123 45 67"));
    }

    @Test
    public void testParsePhone_InvalidPhone() {
        var exception = assertThrows(BadRequestException.class, () -> {
            TextUtils.parsePhone("12345");
        });
        assertEquals("Неверный формат номера телефона", exception.getMessage());

        Assertions.assertDoesNotThrow(() -> TextUtils.parsePhone("+7 (925) 123 45 67"));
    }

    @Test
    public void testParseEmail_ValidEmail() throws BadRequestException {
        assertEquals("example@example.com", TextUtils.parseEmail("example@example.com"));
    }

    @Test
    public void testParseEmail_InvalidEmail() {
        var exception = assertThrows(BadRequestException.class, () -> {
            TextUtils.parseEmail("example@com");
        });
        assertEquals("Неверный формат email", exception.getMessage());

        exception = assertThrows(BadRequestException.class, () -> {
            TextUtils.parseEmail("   ");
        });
        assertEquals("должно быть строкой, представляющей email.", exception.getMessage());
    }

    @Test
    public void testParseStatus_ValidStatus() throws BadRequestException {
        assertEquals(StudentStatus.APPLICATION_RETURNED, TextUtils.parseStatusByDisplayName("Заявка возвращена на доработку"));
        assertEquals(StudentStatus.REGISTERED, TextUtils.parseStatusByDisplayName("Зарегистрирован"));
    }

    @Test
    public void testParseStatus_InvalidStatus() {
        var exception = assertThrows(BadRequestException.class, () -> {
            TextUtils.parseStatusByDisplayName("unknown_status");
        });
        assertEquals("неверный статус", exception.getMessage());

        exception = assertThrows(BadRequestException.class, () -> {
            TextUtils.parseStatusByDisplayName("   ");
        });
        assertEquals("должно быть строкой, представляющей статус.", exception.getMessage());
    }
}
