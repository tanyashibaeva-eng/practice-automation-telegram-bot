package ru.itmo.infra.handler.usecase.user.companyleadinfo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;

import static org.junit.jupiter.api.Assertions.*;

class ChangeLeadFieldCommandTest {

    private static final long CHAT_ID = 100L;
    private final ChangeLeadFieldCommand command = new ChangeLeadFieldCommand();

    @AfterEach
    void tearDown() {
        ContextHolder.endCommand(CHAT_ID);
    }

    @Test
    void executeShouldSetCommandDataAndNextCommand() throws Exception {
        var message = MessageDTO.builder()
                .chatId(CHAT_ID)
                .text("/change_lead_field#field#PHONE")
                .build();

        var result = command.execute(message);

        assertTrue(result.getText().contains("телефон"));
        var data = ContextHolder.getCommandData(CHAT_ID);
        assertEquals(LeadInfoField.PHONE, data);
        assertInstanceOf(ChangeLeadFieldInputCommand.class, ContextHolder.getNextCommand(CHAT_ID));
    }

    @Test
    void executeShouldPromptForFullName() throws Exception {
        var message = MessageDTO.builder()
                .chatId(CHAT_ID)
                .text("/change_lead_field#field#FULLNAME")
                .build();

        var result = command.execute(message);
        assertTrue(result.getText().contains("ФИО"));
        assertEquals(LeadInfoField.FULLNAME, ContextHolder.getCommandData(CHAT_ID));
    }

    @Test
    void executeShouldPromptForLastName() throws Exception {
        var message = MessageDTO.builder()
                .chatId(CHAT_ID)
                .text("/change_lead_field#field#LASTNAME")
                .build();

        var result = command.execute(message);
        assertTrue(result.getText().contains("фамилию"));
        assertEquals(LeadInfoField.LASTNAME, ContextHolder.getCommandData(CHAT_ID));
    }

    @Test
    void executeShouldPromptForFirstName() throws Exception {
        var message = MessageDTO.builder()
                .chatId(CHAT_ID)
                .text("/change_lead_field#field#FIRSTNAME")
                .build();

        var result = command.execute(message);
        assertTrue(result.getText().contains("имя"));
        assertEquals(LeadInfoField.FIRSTNAME, ContextHolder.getCommandData(CHAT_ID));
    }

    @Test
    void executeShouldPromptForPatronymic() throws Exception {
        var message = MessageDTO.builder()
                .chatId(CHAT_ID)
                .text("/change_lead_field#field#PATRONYMIC")
                .build();

        var result = command.execute(message);
        assertTrue(result.getText().contains("отчество"));
        assertEquals(LeadInfoField.PATRONYMIC, ContextHolder.getCommandData(CHAT_ID));
    }

    @Test
    void executeShouldPromptForEmail() throws Exception {
        var message = MessageDTO.builder()
                .chatId(CHAT_ID)
                .text("/change_lead_field#field#EMAIL")
                .build();

        var result = command.execute(message);
        assertTrue(result.getText().contains("email"));
        assertEquals(LeadInfoField.EMAIL, ContextHolder.getCommandData(CHAT_ID));
    }

    @Test
    void executeShouldPromptForJobTitle() throws Exception {
        var message = MessageDTO.builder()
                .chatId(CHAT_ID)
                .text("/change_lead_field#field#JOB_TITLE")
                .build();

        var result = command.execute(message);
        assertTrue(result.getText().contains("должность"));
        assertEquals(LeadInfoField.JOB_TITLE, ContextHolder.getCommandData(CHAT_ID));
    }

    @Test
    void executeShouldHandleUnknownField() {
        var message = MessageDTO.builder()
                .chatId(CHAT_ID)
                .text("/change_lead_field#field#INVALID")
                .build();

        var result = command.execute(message);
        assertTrue(result.getText().contains("Неизвестное поле"));
    }

    @Test
    void nameShouldBeChangeLeadField() {
        assertEquals("/change_lead_field", command.getName());
    }

    @Test
    void shouldNotNeedNextCall() {
        assertFalse(command.isNextCallNeeded());
    }
}
