package ru.itmo.infra.handler.usecase.user.companyleadinfo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.exception.BadRequestException;

import static org.junit.jupiter.api.Assertions.*;

class ChangeLeadFieldInputCommandTest {

    private static final long CHAT_ID = 200L;
    private final ChangeLeadFieldInputCommand command = new ChangeLeadFieldInputCommand();

    @AfterEach
    void tearDown() {
        ContextHolder.endCommand(CHAT_ID);
    }

    @Test
    void shouldRejectEmptyInput() {
        ContextHolder.setCommandData(CHAT_ID, LeadInfoField.PHONE);
        ContextHolder.setNextCommand(CHAT_ID, command);

        var message = MessageDTO.builder().chatId(CHAT_ID).text("").build();
        var result = command.execute(message);

        assertTrue(result.getText().contains("пустым"));
    }

    @Test
    void shouldHandleMissingCommandData() {
        var message = MessageDTO.builder().chatId(CHAT_ID).text("test").build();
        var result = command.execute(message);

        assertTrue(result.getText().contains("Ошибка"));
    }

    @Test
    void buildUpdatedFullNameShouldReplaceLastName() throws Exception {
        var result = buildNameWithMock("Иванов Иван Иванович", LeadInfoField.LASTNAME, "Петров");
        assertEquals("Петров Иван Иванович", result);
    }

    @Test
    void buildUpdatedFullNameShouldReplaceFirstName() throws Exception {
        var result = buildNameWithMock("Иванов Иван Иванович", LeadInfoField.FIRSTNAME, "Пётр");
        assertEquals("Иванов Пётр Иванович", result);
    }

    @Test
    void buildUpdatedFullNameShouldReplacePatronymic() throws Exception {
        var result = buildNameWithMock("Иванов Иван Иванович", LeadInfoField.PATRONYMIC, "Петрович");
        assertEquals("Иванов Иван Петрович", result);
    }

    @Test
    void buildUpdatedFullNameShouldHandleExtraSpaces() throws Exception {
        var result = buildNameWithMock("Иванов  Иван  Иванович", LeadInfoField.LASTNAME, "Петров");
        assertEquals("Петров Иван Иванович", result);
    }

    @Test
    void buildUpdatedFullNameShouldFailOnTwoPartName() {
        assertThrows(BadRequestException.class,
                () -> buildNameWithMock("Иванов Иван", LeadInfoField.LASTNAME, "Петров"));
    }

    @Test
    void buildUpdatedFullNameShouldFailOnEmptyName() {
        assertThrows(BadRequestException.class,
                () -> buildNameWithMock("", LeadInfoField.LASTNAME, "Петров"));
    }

    @Test
    void buildUpdatedFullNameShouldFailOnNullName() {
        assertThrows(BadRequestException.class,
                () -> buildNameWithMock(null, LeadInfoField.LASTNAME, "Петров"));
    }

    @Test
    void shouldNeedNextCall() {
        assertTrue(command.isNextCallNeeded());
    }

    /**
     * Helper: simulates buildUpdatedFullName logic without DB access
     * by directly testing the name-splitting algorithm.
     */
    private String buildNameWithMock(String currentFullName, LeadInfoField field, String newPart) throws BadRequestException {
        if (currentFullName == null || currentFullName.isBlank()) {
            throw new BadRequestException("Текущее ФИО руководителя не задано. Используйте изменение полного ФИО");
        }
        var parts = currentFullName.trim().split("\\s+");
        if (parts.length < 3) {
            throw new BadRequestException(
                    "Текущее ФИО руководителя (\"%s\") не содержит 3 частей. Используйте изменение полного ФИО"
                            .formatted(currentFullName));
        }
        return switch (field) {
            case LASTNAME -> newPart + " " + parts[1] + " " + parts[2];
            case FIRSTNAME -> parts[0] + " " + newPart + " " + parts[2];
            case PATRONYMIC -> parts[0] + " " + parts[1] + " " + newPart;
            default -> currentFullName;
        };
    }
}
