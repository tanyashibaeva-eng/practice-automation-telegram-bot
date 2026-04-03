package ru.itmo.infra.handler.usecase.admin.searchstudent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.domain.model.EduStream;
import ru.itmo.domain.model.Student;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.domain.type.PracticePlace;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.BadRequestException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchStudentCommandTest {

    private static final long CHAT_ID = 300L;

    @AfterEach
    void tearDown() {
        ContextHolder.endCommand(CHAT_ID);
    }

    @Test
    void searchByIsuShouldRequireArgument() {
        var command = new SearchStudentByIsuCommand();
        ContextHolder.setEduStreamName(CHAT_ID, "test-stream");

        var message = MessageDTO.builder()
                .chatId(CHAT_ID)
                .text("/search_by_isu")
                .build();

        var result = command.execute(message);
        assertTrue(result.getText().contains("Формат"));
    }

    @Test
    void searchByIsuShouldRejectNonNumericIsu() {
        var command = new SearchStudentByIsuCommand();
        ContextHolder.setEduStreamName(CHAT_ID, "test-stream");

        var message = MessageDTO.builder()
                .chatId(CHAT_ID)
                .text("/search_by_isu abc")
                .build();

        var result = command.execute(message);
        assertTrue(result.getText().contains("Неверный формат"));
    }

    @Test
    void searchByIsuShouldRequireStream() {
        var command = new SearchStudentByIsuCommand();

        var message = MessageDTO.builder()
                .chatId(CHAT_ID)
                .text("/search_by_isu 123456")
                .build();

        var result = command.execute(message);
        assertTrue(result.getText().contains("поток"));
    }

    @Test
    void searchByGroupShouldRequireGroupAndFio() {
        var command = new SearchStudentByGroupCommand();
        ContextHolder.setEduStreamName(CHAT_ID, "test-stream");

        var message = MessageDTO.builder()
                .chatId(CHAT_ID)
                .text("/search_by_group")
                .build();

        var result = command.execute(message);
        assertTrue(result.getText().contains("Формат"));
    }

    @Test
    void searchByGroupShouldRequireFio() {
        var command = new SearchStudentByGroupCommand();
        ContextHolder.setEduStreamName(CHAT_ID, "test-stream");

        var message = MessageDTO.builder()
                .chatId(CHAT_ID)
                .text("/search_by_group M3100")
                .build();

        var result = command.execute(message);
        assertTrue(result.getText().contains("Формат"));
    }

    @Test
    void searchByGroupShouldRequireStream() {
        var command = new SearchStudentByGroupCommand();

        var message = MessageDTO.builder()
                .chatId(CHAT_ID)
                .text("/search_by_group M3100 Иванов")
                .build();

        var result = command.execute(message);
        assertTrue(result.getText().contains("поток"));
    }

    @Test
    void formatStudentListShouldContainStudentInfo() throws BadRequestException {
        var student = new Student(
                null, new EduStream("test-stream"), 123456, "M3100", "Иванов Иван Иванович",
                StudentStatus.COMPANY_INFO_WAITING_APPROVAL,
                "", "", PracticePlace.OTHER_COMPANY, PracticeFormat.OFFLINE,
                7812345678L, "ООО Тест", "Руководитель Тест",
                "+7 999 999 9999", "lead@test.com", "CTO",
                "FFFFFF", false, null, null, null, null, false
        );

        var result = SearchStudentByIsuCommand.formatStudentList(List.of(student));

        assertTrue(result.contains("Иванов Иван Иванович"));
        assertTrue(result.contains("123456"));
        assertTrue(result.contains("M3100"));
        assertTrue(result.contains("ООО Тест"));
        assertTrue(result.contains("Руководитель Тест"));
        assertTrue(result.contains("+7 999 999 9999"));
        assertTrue(result.contains("lead@test.com"));
        assertTrue(result.contains("CTO"));
    }

    @Test
    void formatStudentListShouldHandleEmptyLeadFields() throws BadRequestException {
        var student = new Student(
                null, new EduStream("test-stream"), 123456, "M3100", "Иванов Иван",
                StudentStatus.REGISTERED,
                "", "", PracticePlace.NOT_SPECIFIED, PracticeFormat.NOT_SPECIFIED,
                null, null, null, null, null, null,
                "FFFFFF", false, null, null, null, null, false
        );

        var result = SearchStudentByIsuCommand.formatStudentList(List.of(student));

        assertTrue(result.contains("Иванов Иван"));
        assertTrue(result.contains("123456"));
        assertFalse(result.contains("Компания:"));
        assertFalse(result.contains("Руководитель:"));
    }

    @Test
    void searchByIsuNameShouldBeCorrect() {
        assertEquals("/search_by_isu", new SearchStudentByIsuCommand().getName());
    }

    @Test
    void searchByGroupNameShouldBeCorrect() {
        assertEquals("/search_by_group", new SearchStudentByGroupCommand().getName());
    }

    @Test
    void searchByIsuShouldBeAdminCommand() {
        assertTrue(new SearchStudentByIsuCommand().isAdminCommand());
    }

    @Test
    void searchByGroupShouldBeAdminCommand() {
        assertTrue(new SearchStudentByGroupCommand().isAdminCommand());
    }

    @Test
    void searchByIsuDescriptionShouldNotBeEmpty() {
        assertFalse(new SearchStudentByIsuCommand().getDescription().isEmpty());
    }

    @Test
    void searchByGroupDescriptionShouldNotBeEmpty() {
        assertFalse(new SearchStudentByGroupCommand().getDescription().isEmpty());
    }
}
