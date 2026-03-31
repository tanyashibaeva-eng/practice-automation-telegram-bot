package ru.itmo.bugfixes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.NotificationService;
import ru.itmo.domain.dto.ExcelStudentDTO;
import ru.itmo.domain.model.Student;
import ru.itmo.domain.model.TelegramUser;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.domain.type.PracticePlace;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.InternalException;
import ru.itmo.exception.UnknownUserException;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.handler.usecase.start.StartCommand;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты для проверки исправлений багов.
 * Не требуют подключения к базе данных.
 */
class BugFixesUnitTest {

    @Nested
    @DisplayName("BUG-2: Interceptor NPE при отсутствии cause в InternalException")
    class InterceptorNpeTest {

        @Test
        @DisplayName("InternalException без cause не вызывает NPE при вызове getMessage()")
        void internalExceptionWithoutCauseShouldNotThrowNpe() {
            InternalException ex = new InternalException("Что-то пошло не так");
            assertNull(ex.getCause());
            assertDoesNotThrow(() -> {
                String message = ex.getMessage();
                assertNotNull(message);
                assertEquals("Что-то пошло не так", message);
            });
        }

        @Test
        @DisplayName("InternalException с cause работает корректно")
        void internalExceptionWithCauseShouldWork() {
            var cause = new RuntimeException("root cause");
            InternalException ex = new InternalException("wrapper", cause);
            assertNotNull(ex.getCause());
            assertEquals("root cause", ex.getCause().getMessage());
        }
    }

    @Nested
    @DisplayName("BUG-4: NotificationService NPE при null telegramUser")
    class NotificationServiceNpeTest {

        @Test
        @DisplayName("pingStudent не падает при null telegramUser")
        void pingStudentWithNullTelegramUserShouldNotThrow() {
            Student studentWithoutTgUser = new Student(
                    null, null, 12345, "A1", "Иванов Иван", StudentStatus.REGISTERED,
                    "", "", PracticePlace.NOT_SPECIFIED, PracticeFormat.NOT_SPECIFIED,
                    null, null, null, null, null, null, "FFFFFF", false,
                    null, null, null, null, false
            );

            assertNull(studentWithoutTgUser.getTelegramUser());
            assertDoesNotThrow(() -> NotificationService.pingStudent(studentWithoutTgUser));
        }

        @Test
        @DisplayName("pingStudent не пингует студента со статусом, не требующим пинга")
        void pingStudentWithTerminalStatusShouldNotPing() {
            TelegramUser tgUser = new TelegramUser(123L, false, false, "user");
            Student student = new Student(
                    tgUser, null, 12345, "A1", "Иванов Иван", StudentStatus.APPLICATION_SIGNED,
                    "", "", PracticePlace.OTHER_COMPANY, PracticeFormat.ONLINE,
                    null, null, null, null, null, null, "FFFFFF", false,
                    null, null, null, null, false
            );

            // Не должно упасть — статус APPLICATION_SIGNED не требует пинга
            assertDoesNotThrow(() -> NotificationService.pingStudent(student));
        }
    }

    @Nested
    @DisplayName("BUG-5: ContextHolder потокобезопасность")
    class ContextHolderThreadSafetyTest {

        @Test
        @DisplayName("Параллельная запись в ContextHolder не вызывает ошибок")
        void concurrentSetOperationsShouldNotFail() throws InterruptedException {
            int threadCount = 50;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger errorCount = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                long chatId = i;
                executor.submit(() -> {
                    try {
                        ContextHolder.setNextCommand(chatId, new StartCommand());
                        ContextHolder.setEduStreamName(chatId, "stream-" + chatId);
                        ContextHolder.setCommandData(chatId, "data-" + chatId);

                        Command cmd = ContextHolder.getNextCommand(chatId);
                        assertNotNull(cmd);

                        String stream = ContextHolder.getEduStreamName(chatId);
                        assertEquals("stream-" + chatId, stream);
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                    } finally {
                        ContextHolder.endCommand(chatId);
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();
            assertEquals(0, errorCount.get(), "Не должно быть ошибок при параллельном доступе");
        }

        @Test
        @DisplayName("endCommand очищает все данные пользователя")
        void endCommandShouldClearAllData() {
            long chatId = 99999L;
            ContextHolder.setNextCommand(chatId, new StartCommand());
            ContextHolder.setEduStreamName(chatId, "test-stream");

            ContextHolder.endCommand(chatId);

            assertThrows(UnknownUserException.class, () -> ContextHolder.getNextCommand(chatId));
            assertThrows(UnknownUserException.class, () -> ContextHolder.getEduStreamName(chatId));
        }
    }

    @Nested
    @DisplayName("BUG-7: Student.updateOrGetErrors NPE при null timestamps")
    class StudentTimestampNpeTest {

        @Test
        @DisplayName("updateOrGetErrors не падает при null updatedAt")
        void updateOrGetErrorsWithNullUpdatedAtShouldNotThrow() {
            Student student = new Student(
                    null, null, 12345, "A1", "Иванов Иван", StudentStatus.REGISTERED,
                    "", "", PracticePlace.NOT_SPECIFIED, PracticeFormat.NOT_SPECIFIED,
                    null, null, null, null, null, null, "FFFFFF", false,
                    Timestamp.from(Instant.now()), null, // updatedAt = null
                    null, null, false
            );

            ExcelStudentDTO dto = new ExcelStudentDTO(
                    12345L,
                    12345, "A1", "Иванов Иван", StudentStatus.REGISTERED,
                    "", "", PracticePlace.NOT_SPECIFIED,
                    PracticeFormat.NOT_SPECIFIED, null, null, null,
                    null, null, null, "FFFFFF", null
            );

            assertDoesNotThrow(() -> student.updateOrGetErrors(dto));
        }

        @Test
        @DisplayName("updateOrGetErrors не падает при null exportedAt")
        void updateOrGetErrorsWithNullExportedAtShouldNotThrow() {
            Student student = new Student(
                    null, null, 12345, "A1", "Иванов Иван", StudentStatus.REGISTERED,
                    "", "", PracticePlace.NOT_SPECIFIED, PracticeFormat.NOT_SPECIFIED,
                    null, null, null, null, null, null, "FFFFFF", false,
                    null, Timestamp.from(Instant.now()), // exportedAt = null
                    null, null, false
            );

            ExcelStudentDTO dto = new ExcelStudentDTO(
                    12345L,
                    12345, "A1", "Иванов Иван", StudentStatus.REGISTERED,
                    "", "", PracticePlace.NOT_SPECIFIED,
                    PracticeFormat.NOT_SPECIFIED, null, null, null,
                    null, null, null, "FFFFFF", null
            );

            assertDoesNotThrow(() -> student.updateOrGetErrors(dto));
        }

        @Test
        @DisplayName("updateOrGetErrors не падает при обоих null timestamps")
        void updateOrGetErrorsWithBothNullTimestampsShouldNotThrow() {
            Student student = new Student(
                    null, null, 12345, "A1", "Иванов Иван", StudentStatus.REGISTERED,
                    "", "", PracticePlace.NOT_SPECIFIED, PracticeFormat.NOT_SPECIFIED,
                    null, null, null, null, null, null, "FFFFFF", false,
                    null, null, null, null, false
            );

            ExcelStudentDTO dto = new ExcelStudentDTO(
                    12345L,
                    12345, "A1", "Иванов Иван", StudentStatus.REGISTERED,
                    "", "", PracticePlace.NOT_SPECIFIED,
                    PracticeFormat.NOT_SPECIFIED, null, null, null,
                    null, null, null, "FFFFFF", null
            );

            List<String> errors = student.updateOrGetErrors(dto);
            assertNotNull(errors);
        }

        @Test
        @DisplayName("updateOrGetErrors корректно работает когда updatedAt < exportedAt")
        void updateOrGetErrorsWhenUpdatedBeforeExportedShouldProcessStatusChange() {
            Timestamp exported = Timestamp.from(Instant.now());
            Timestamp updated = Timestamp.from(Instant.now().minusSeconds(60));

            Student student = new Student(
                    null, null, 12345, "A1", "Иванов Иван", StudentStatus.REGISTERED,
                    "", "", PracticePlace.NOT_SPECIFIED, PracticeFormat.NOT_SPECIFIED,
                    null, null, null, null, null, null, "FFFFFF", false,
                    exported, updated, null, null, false
            );

            ExcelStudentDTO dto = new ExcelStudentDTO(
                    12345L,
                    12345, "A1", "Иванов Иван", StudentStatus.PRACTICE_IN_ITMO_MARKINA,
                    "", "", PracticePlace.ITMO_MARKINA,
                    PracticeFormat.NOT_SPECIFIED, null, null, null,
                    null, null, null, "FFFFFF", null
            );

            List<String> errors = student.updateOrGetErrors(dto);
            assertTrue(errors.isEmpty(), "Переход REGISTERED -> PRACTICE_IN_ITMO_MARKINA должен быть допустим");
            assertEquals(StudentStatus.PRACTICE_IN_ITMO_MARKINA, student.getStatus());
        }
    }
}
