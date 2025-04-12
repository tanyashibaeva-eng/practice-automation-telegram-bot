package ru.itmo.application;

import org.junit.jupiter.api.*;
import ru.itmo.domain.dto.command.UserRegistrationArgs;
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
import ru.itmo.infra.storage.TelegramUserRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BanUserTest {

    private static final TelegramUser telegramUser;
    private static final EduStream eduStream;
    private static final Student student;

    static {
        telegramUser = new TelegramUser(123, false, false, "user");

        try {
            eduStream = new EduStream(
                    "stream",
                    2023,
                    LocalDate.of(LocalDate.now().getYear(), 1, 1),
                    LocalDate.of(LocalDate.now().getYear() + 1, 1, 1)
            );
        } catch (BadRequestException ex) {
            throw new RuntimeException(ex);
        }

        student = new Student(
                telegramUser,
                eduStream,
                2,
                "G1",
                "name 2",
                StudentStatus.COMPANY_INFO_WAITING_APPROVAL,
                "comments 2",
                "call status comments 2",
                PracticePlace.ITMO_UNIVERSITY,
                PracticeFormat.ONLINE,
                78222L,
                "company name 2",
                "company lead full name 2",
                "+7 phone 2",
                "email 2",
                "manager 2",
                "2",
                false,
                null
        );

        try {
            TelegramUserRepository.save(telegramUser);
            EduStreamRepository.save(eduStream);
            StudentRepository.saveBaseBatch(List.of(student));
            TelegramUserService.registerUser(new UserRegistrationArgs(
                    telegramUser.getChatId(),
                    telegramUser.getUsername(),
                    student.getEduStream().getName(),
                    student.getIsu()
            ));
            StudentRepository.updateBatchByChatIdAndEduStreamName(List.of(student));
        } catch (InternalException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Order(1)
    @Test
    void banUserTest() throws BadRequestException, InternalException {
        Assertions.assertDoesNotThrow(() -> TelegramUserService.banUser(telegramUser));
        Optional<TelegramUser> telegramUserOpt = TelegramUserService.findByChatId(telegramUser.getChatId());
        Assertions.assertTrue(telegramUserOpt.isPresent() && telegramUserOpt.get().isBanned());
        Assertions.assertEquals(
                student.duplicateBase(),
                StudentService.findAllStudentsByIsuAndEduStreamName(student.getIsu(), eduStream.getName()).get(0)
        );
    }

    @Order(2)
    @Test
    void unbanUserTest() throws InternalException {
        Assertions.assertDoesNotThrow(() -> TelegramUserService.unbanUser(telegramUser));
        Optional<TelegramUser> telegramUserOpt = TelegramUserService.findByChatId(telegramUser.getChatId());
        Assertions.assertTrue(telegramUserOpt.isPresent() && !telegramUserOpt.get().isBanned());
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
