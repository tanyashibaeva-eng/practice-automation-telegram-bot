package ru.itmo.infra.storage;

import org.junit.jupiter.api.*;
import ru.itmo.domain.model.EduStream;
import ru.itmo.domain.model.Student;
import ru.itmo.domain.model.TelegramUser;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.domain.type.PracticePlace;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SignedPhotoRepositoryTest {

    private static EduStream eduStream;
    private static final long CHAT_ID = 99999L;
    private static final int ISU = 999999;
    private static final String STREAM_NAME = "photo_repo_test";

    @BeforeAll
    static void setup() throws BadRequestException, InternalException, SQLException {
        eduStream = new EduStream(STREAM_NAME, 2026,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31));

        EduStreamRepository.save(eduStream);

        Connection connection = DatabaseManager.getConnection();
        try (var stmt = connection.prepareStatement(
                "INSERT INTO tg_user (chat_id, is_admin, is_banned, username) VALUES (?, false, false, ?) ON CONFLICT DO NOTHING;"
        )) {
            stmt.setLong(1, CHAT_ID);
            stmt.setString(2, "test_photo_user");
            stmt.executeUpdate();
        }

        try (var stmt = connection.prepareStatement("""
                INSERT INTO student (chat_id, edu_stream_name, isu, st_group, fullname, status)
                VALUES (?, ?, ?, 'P3433', 'Тест Фото Студент', ?)
                ON CONFLICT DO NOTHING;
                """)) {
            stmt.setLong(1, CHAT_ID);
            stmt.setString(2, STREAM_NAME);
            stmt.setInt(3, ISU);
            stmt.setObject(4, StudentStatus.APPLICATION_WAITING_SIGNING, Types.OTHER);
            stmt.executeUpdate();
        }
    }

    @Test
    @Order(1)
    void studentExistsWithCorrectStatus() throws InternalException {
        Optional<Student> studentOpt = StudentRepository.findByChatIdAndEduStreamName(CHAT_ID, eduStream);
        assertTrue(studentOpt.isPresent(), "Студент должен существовать");

        Student student = studentOpt.get();
        assertEquals(StudentStatus.APPLICATION_WAITING_SIGNING, student.getStatus());
        assertNull(student.getSignedPhotoPath(), "signedPhotoPath должен быть null до загрузки");
    }

    @Test
    @Order(2)
    void updateSignedPhotoPath_success() throws InternalException {
        String photoPath = "signed_photos/photo_repo_test/99999_1234567890.jpg";

        boolean updated = StudentRepository.updateSignedPhotoPath(CHAT_ID, STREAM_NAME, photoPath);
        assertTrue(updated, "Обновление должно пройти успешно");

        Optional<Student> studentOpt = StudentRepository.findByChatIdAndEduStreamName(CHAT_ID, eduStream);
        assertTrue(studentOpt.isPresent());

        Student student = studentOpt.get();
        assertEquals(photoPath, student.getSignedPhotoPath(),
                "Путь к фото должен быть сохранён");
        assertEquals(StudentStatus.APPLICATION_PHOTO_UPLOADED, student.getStatus(),
                "Статус должен измениться на APPLICATION_PHOTO_UPLOADED");
    }

    @Test
    @Order(3)
    void updateSignedPhotoPath_overwrite() throws InternalException {
        String newPhotoPath = "signed_photos/photo_repo_test/99999_9999999999.png";

        Connection connection = DatabaseManager.getConnection();
        try (var stmt = connection.prepareStatement(
                "UPDATE student SET status = ? WHERE chat_id = ? AND edu_stream_name = ?;"
        )) {
            stmt.setObject(1, StudentStatus.APPLICATION_WAITING_SIGNING, Types.OTHER);
            stmt.setLong(2, CHAT_ID);
            stmt.setString(3, STREAM_NAME);
            stmt.executeUpdate();
        } catch (SQLException e) {
            fail("Не удалось обновить статус: " + e.getMessage());
        }

        boolean updated = StudentRepository.updateSignedPhotoPath(CHAT_ID, STREAM_NAME, newPhotoPath);
        assertTrue(updated);

        Optional<Student> studentOpt = StudentRepository.findByChatIdAndEduStreamName(CHAT_ID, eduStream);
        assertTrue(studentOpt.isPresent());
        assertEquals(newPhotoPath, studentOpt.get().getSignedPhotoPath(),
                "Путь должен быть обновлён на новый");
    }

    @Test
    @Order(4)
    void updateSignedPhotoPath_nonExistentStudent() throws InternalException {
        boolean updated = StudentRepository.updateSignedPhotoPath(
                123456789L, STREAM_NAME, "some/path.jpg");
        assertFalse(updated, "Обновление несуществующего студента должно вернуть false");
    }

    @Test
    @Order(5)
    void updateSignedPhotoPath_nonExistentStream() throws InternalException {
        boolean updated = StudentRepository.updateSignedPhotoPath(
                CHAT_ID, "nonexistent_stream", "some/path.jpg");
        assertFalse(updated, "Обновление с несуществующим потоком должно вернуть false");
    }

    @Test
    @Order(6)
    void signedPhotoPathPreservedInFindAll() throws InternalException {
        List<Student> allStudents = StudentRepository.findAll();

        Student photoStudent = null;
        for (Student s : allStudents) {
            if (s.getIsu() == ISU) {
                photoStudent = s;
                break;
            }
        }
        assertNotNull(photoStudent, "Студент должен быть в findAll");
        assertNotNull(photoStudent.getSignedPhotoPath(),
                "signedPhotoPath должен читаться из БД через findAll");
    }

    @Test
    @Order(7)
    void signedPhotoPathPreservedInFindAllByChatId() throws InternalException {
        List<Student> students = StudentRepository.findAllByChatId(CHAT_ID);
        assertFalse(students.isEmpty());
        assertNotNull(students.get(0).getSignedPhotoPath(),
                "signedPhotoPath должен читаться через findAllByChatId");
    }

    @AfterAll
    static void teardown() throws SQLException {
        Connection connection = DatabaseManager.getConnection();
        try (var stmt = connection.prepareStatement(
                "DELETE FROM student WHERE isu = ?;"
        )) {
            stmt.setInt(1, ISU);
            stmt.executeUpdate();
        }
        try (var stmt = connection.prepareStatement(
                "DELETE FROM tg_user WHERE chat_id = ?;"
        )) {
            stmt.setLong(1, CHAT_ID);
            stmt.executeUpdate();
        }
        try (var stmt = connection.prepareStatement(
                "DELETE FROM edu_stream WHERE name = ?;"
        )) {
            stmt.setString(1, STREAM_NAME);
            stmt.executeUpdate();
        }
    }
}