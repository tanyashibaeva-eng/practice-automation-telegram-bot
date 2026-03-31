package ru.itmo.bugfixes;

import org.junit.jupiter.api.*;
import ru.itmo.application.NotificationService;
import ru.itmo.application.TelegramUserService;
import ru.itmo.application.AdminTokenService;
import ru.itmo.domain.dto.command.UserRegistrationArgs;
import ru.itmo.domain.model.AdminToken;
import ru.itmo.domain.model.EduStream;
import ru.itmo.domain.model.Student;
import ru.itmo.domain.model.TelegramUser;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.domain.type.PracticePlace;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.storage.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Интеграционные тесты для проверки исправлений багов.
 * Требуют подключения к PostgreSQL (PG_DSN, BOT_TOKEN в .env).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BugFixesIntegrationTest {

    private static final String STREAM_NAME = "bugfix-test-stream";
    private static EduStream eduStream;

    @BeforeAll
    static void setup() throws BadRequestException, InternalException, SQLException {
        eduStream = new EduStream(
                STREAM_NAME,
                2026,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2027, 1, 1)
        );
        EduStreamRepository.save(eduStream);
    }

    // =========================================================================
    // BUG-1: Thread-safe DB connections
    // Проверяем, что параллельные операции с БД не ломают друг друга
    // =========================================================================

    @Order(1)
    @Test
    @DisplayName("BUG-1: Параллельные запросы к БД не вызывают ошибок")
    void concurrentDatabaseAccessShouldNotFail() throws InterruptedException, InternalException, SQLException, BadRequestException {
        // Создаём пачку студентов
        List<Student> students = new ArrayList<>();
        for (int i = 100; i < 120; i++) {
            students.add(new Student(
                    null, eduStream, i, "G1", "Student " + i,
                    StudentStatus.NOT_REGISTERED, "", "",
                    PracticePlace.NOT_SPECIFIED, PracticeFormat.NOT_SPECIFIED,
                    null, null, null, null, null, null,
                    "FFFFFF", false, null, null, null, null, false
            ));
        }
        StudentRepository.saveBaseBatch(students);

        // Запускаем параллельные чтения
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    // Каждый поток делает несколько запросов
                    List<Student> all = StudentRepository.findAll(
                            Filter.builder().eduStream(eduStream).build()
                    );
                    Assertions.assertFalse(all.isEmpty());

                    EduStreamRepository.findAll();
                    EduStreamRepository.findByName(eduStream);

                    StudentRepository.findAllByIsuAndEduStreamName(100, eduStream);
                } catch (Exception e) {
                    e.printStackTrace();
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        Assertions.assertEquals(0, errorCount.get(),
                "Параллельные запросы к БД не должны вызывать ошибок");
    }

    // =========================================================================
    // BUG-3: updateByIsuAndEduStream больше не закрывает общее соединение
    // =========================================================================

    @Order(2)
    @Test
    @DisplayName("BUG-3: updateByIsuAndEduStream не ломает последующие запросы")
    void updateByIsuAndEduStreamShouldNotBreakSubsequentQueries() throws InternalException, BadRequestException {
        // Вызываем updateByIsuAndEduStream
        Student toUpdate = new Student(
                null, eduStream, 100, "G1", "Updated Student 100",
                StudentStatus.NOT_REGISTERED, "", "",
                PracticePlace.NOT_SPECIFIED, PracticeFormat.NOT_SPECIFIED,
                null, null, null, null, null, null,
                "FFFFFF", false, null, null, null, null, false
        );
        StudentRepository.updateByIsuAndEduStream(List.of(toUpdate));

        // После этого БД должна продолжать работать
        List<Student> all = StudentRepository.findAll();
        Assertions.assertFalse(all.isEmpty(), "findAll должен работать после updateByIsuAndEduStream");

        List<EduStream> streams = EduStreamRepository.findAll();
        Assertions.assertFalse(streams.isEmpty(), "EduStreamRepository.findAll должен работать");

        // Проверяем, что обновление прошло
        List<Student> found = StudentRepository.findAllByIsuAndEduStreamName(100, eduStream);
        Assertions.assertFalse(found.isEmpty());
        Assertions.assertEquals("Updated Student 100", found.get(0).getFullName());
    }

    // =========================================================================
    // BUG-4: pingStudent с null telegramUser (интеграционный)
    // =========================================================================

    @Order(3)
    @Test
    @DisplayName("BUG-4: pingStudents не падает при наличии студентов без chat_id")
    void pingStudentsShouldNotFailWithNullChatIdStudents() throws InternalException, SQLException {
        // Создаём студента в статусе REGISTERED но без chat_id (краевой случай)
        try (var connection = DatabaseManager.getConnection();
             var stmt = connection.prepareStatement(
                     "UPDATE student SET status = CAST('REGISTERED' AS st_status) WHERE isu = 101"
             )) {
            stmt.executeUpdate();
        }

        // pingStudents не должен бросать NPE несмотря на студента без chat_id
        Assertions.assertDoesNotThrow(() -> NotificationService.pingStudents());

        // Вернём обратно
        try (var connection = DatabaseManager.getConnection();
             var stmt = connection.prepareStatement(
                     "UPDATE student SET status = CAST('NOT_REGISTERED' AS st_status) WHERE isu = 101"
             )) {
            stmt.executeUpdate();
        }
    }

    // =========================================================================
    // BUG-6: SQL Injection в buildFilteringQuery
    // =========================================================================

    @Order(4)
    @Test
    @DisplayName("BUG-6: Фильтрация с спецсимволами не ломает запрос")
    void filterWithSpecialCharactersShouldNotBreak() throws InternalException, BadRequestException {
        // Имя потока с кавычкой — раньше вызвало бы SQL ошибку
        // EduStream валидирует длину < 21, поэтому используем короткую строку
        Filter filterWithQuote = Filter.builder()
                .eduStream(new EduStream("s'; DROP TABLE x;"))
                .build();

        // Не должно бросить SQL ошибку, просто вернёт пустой список
        List<Student> result = StudentRepository.findAll(filterWithQuote);
        Assertions.assertTrue(result.isEmpty());

        // Группа с кавычкой
        Filter filterWithQuoteGroup = Filter.builder()
                .eduStream(eduStream)
                .stGroups(List.of("G1' OR '1'='1"))
                .build();

        result = StudentRepository.findAll(filterWithQuoteGroup);
        Assertions.assertTrue(result.isEmpty(),
                "Фильтр с SQL-инъекцией в группе не должен вернуть все записи");
    }

    @Order(5)
    @Test
    @DisplayName("BUG-6: Нормальная фильтрация по-прежнему работает корректно")
    void filterShouldStillWorkCorrectly() throws InternalException {
        // Фильтрация по потоку
        Filter filter = Filter.builder()
                .eduStream(eduStream)
                .build();
        List<Student> result = StudentRepository.findAll(filter);
        Assertions.assertFalse(result.isEmpty());

        // Фильтрация по потоку + группе
        filter = Filter.builder()
                .eduStream(eduStream)
                .stGroups(List.of("G1"))
                .build();
        result = StudentRepository.findAll(filter);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertTrue(result.stream().allMatch(s -> s.getStGroup().equals("G1")));

        // Фильтрация по статусу
        filter = Filter.builder()
                .stStatuses(List.of(StudentStatus.NOT_REGISTERED))
                .build();
        result = StudentRepository.findAll(filter);
        Assertions.assertFalse(result.isEmpty());

        // Фильтрация по несуществующей группе
        filter = Filter.builder()
                .eduStream(eduStream)
                .stGroups(List.of("NONEXISTENT_GROUP"))
                .build();
        result = StudentRepository.findAll(filter);
        Assertions.assertTrue(result.isEmpty());
    }

    // =========================================================================
    // BUG-1 (доп.): Регистрация пользователя с транзакцией через отдельное соединение
    // =========================================================================

    @Order(6)
    @Test
    @DisplayName("BUG-1: Транзакционная регистрация пользователя работает корректно")
    void transactionalUserRegistrationShouldWork() throws InternalException, BadRequestException {
        TelegramUser adminUser = new TelegramUser(9000, false, false, "admin-bugfix");
        AdminToken token = AdminTokenService.generateToken();
        TelegramUserService.registerAdmin(adminUser, token);

        // Регистрация студента
        var result = TelegramUserService.registerUser(new UserRegistrationArgs(
                9001L, "student-bugfix", STREAM_NAME, 100
        ));
        Assertions.assertTrue(result.getErrorText().isEmpty(),
                "Регистрация студента должна пройти без ошибок: " + result.getErrorText());

        // Проверяем что студент привязан
        var studentOpt = StudentRepository.findByChatIdAndEduStreamName(9001L, eduStream);
        Assertions.assertTrue(studentOpt.isPresent());
        Assertions.assertEquals(9001L, studentOpt.get().getTelegramUser().getChatId());
    }

    // =========================================================================
    // BUG-1 (доп.): Бан пользователя с транзакцией
    // =========================================================================

    @Order(7)
    @Test
    @DisplayName("BUG-1: Транзакционный бан пользователя работает корректно")
    void transactionalBanUserShouldWork() throws InternalException, BadRequestException {
        boolean banned = TelegramUserService.banUser(9001L);
        Assertions.assertTrue(banned);

        var tgUser = TelegramUserService.findByChatId(9001L);
        Assertions.assertTrue(tgUser.isPresent());
        Assertions.assertTrue(tgUser.get().isBanned());

        // Разбан
        boolean unbanned = TelegramUserService.unbanUser(9001L);
        Assertions.assertTrue(unbanned);

        tgUser = TelegramUserService.findByChatId(9001L);
        Assertions.assertTrue(tgUser.isPresent());
        Assertions.assertFalse(tgUser.get().isBanned());
    }

    @AfterAll
    static void teardown() throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(
                     "TRUNCATE TABLE student, tg_user, edu_stream, admin_token RESTART IDENTITY CASCADE;"
             )) {
            statement.executeUpdate();
        }
    }
}
