package ru.itmo.application;

import org.junit.jupiter.api.*;
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
import ru.itmo.infra.storage.DatabaseManager;
import ru.itmo.infra.storage.EduStreamRepository;
import ru.itmo.infra.storage.StudentRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserRegistrationTest {
    private static final TelegramUser adminUser = new TelegramUser(1, false, false, "admin");

    private static final TelegramUser userA;
    private static final TelegramUser userB;

    private static final EduStream streamA;
    private static final EduStream streamB;

    private static Student studentAStreamA;
    private static final Student studentAStreamB;

    static {
        try {
            AdminToken adminToken = AdminTokenService.generateToken();
            TelegramUserService.registerAdmin(adminUser, adminToken);

            userA = new TelegramUser(2, false, false, "user A");
            userB = new TelegramUser(3, false, false, "user B");

            streamA = new EduStream(
                    "stream A",
                    2020,
                    LocalDate.of(2020, 1, 1),
                    LocalDate.of(2021, 1, 1));
            streamB = new EduStream(
                    "stream B",
                    2025,
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2026, 1, 1));

            studentAStreamA = new Student(
                    null,
                    streamA,
                    100000,
                    "G1",
                    "student A",
                    StudentStatus.NOT_REGISTERED,
                    "",
                    "",
                    PracticePlace.NOT_SPECIFIED,
                    PracticeFormat.NOT_SPECIFIED,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    false
            );
            studentAStreamB = new Student(
                    null,
                    streamB,
                    200000,
                    "G1",
                    "student B",
                    StudentStatus.NOT_REGISTERED,
                    "",
                    "",
                    PracticePlace.NOT_SPECIFIED,
                    PracticeFormat.NOT_SPECIFIED,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    false
            );

            EduStreamRepository.save(streamA);
            EduStreamRepository.save(streamB);
            StudentRepository.saveBatch(List.of(studentAStreamA, studentAStreamB));

        } catch (BadRequestException | InternalException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Order(1)
    @Test
    void registerUserTest_userIsAdmin_fail() throws InternalException {
        Assertions.assertFalse(
                TelegramUserService.registerUser(new UserRegistrationArgs(
                        adminUser.getChatId(),
                        adminUser.getUsername(),
                        studentAStreamA.getEduStream().getName(),
                        studentAStreamA.getIsu()
                )).getErrorText().isEmpty()
        );
    }

    @Order(2)
    @Test
    void registerUserTest_ok() throws InternalException {
        Assertions.assertTrue(
                TelegramUserService.registerUser(new UserRegistrationArgs(
                        userA.getChatId(),
                        userA.getUsername(),
                        studentAStreamA.getEduStream().getName(),
                        studentAStreamA.getIsu()
                )).getErrorText().isEmpty()
        );

        Optional<Student> resultStudentOpt = StudentRepository.findByChatIdAndEduStreamName(userA.getChatId(), streamA);
        Assertions.assertTrue(resultStudentOpt.isPresent());
        Assertions.assertEquals(resultStudentOpt.get().getTelegramUser(), userA);

        studentAStreamA = resultStudentOpt.get();
    }

    @Order(3)
    @Test
    void registerDuplicateStudent_ok() throws InternalException {
        Assertions.assertTrue(
                TelegramUserService.registerUser(new UserRegistrationArgs(
                        userB.getChatId(),
                        userB.getUsername(),
                        studentAStreamA.getEduStream().getName(),
                        studentAStreamA.getIsu()
                )).getErrorText().isEmpty()
        );

        Optional<Student> resultStudentOpt = StudentRepository.findByChatIdAndEduStreamName(userB.getChatId(), streamA);
        Assertions.assertTrue(resultStudentOpt.isPresent());
        Assertions.assertEquals(resultStudentOpt.get().getTelegramUser(), userB);
    }

    @Order(4)
    @Test
    void registerSameUserOnMultipleStreams_ok() throws InternalException {
        Assertions.assertTrue(
                TelegramUserService.registerUser(new UserRegistrationArgs(
                        userA.getChatId(),
                        userA.getUsername(),
                        studentAStreamB.getEduStream().getName(),
                        studentAStreamB.getIsu()
                )).getErrorText().isEmpty()
        );

        Optional<Student> resultStudentOpt = StudentRepository.findByChatIdAndEduStreamName(userA.getChatId(), streamB);
        Assertions.assertTrue(resultStudentOpt.isPresent());
        Assertions.assertEquals(resultStudentOpt.get().getTelegramUser(), userA);
    }

    @AfterAll
    static void teardown() throws SQLException {
        Connection connection = DatabaseManager.getConnection();

        try (var statement = connection.prepareStatement(
                "TRUNCATE TABLE student, tg_user, edu_stream RESTART IDENTITY;"
        )) {
            statement.executeUpdate();
        }
    }

}
