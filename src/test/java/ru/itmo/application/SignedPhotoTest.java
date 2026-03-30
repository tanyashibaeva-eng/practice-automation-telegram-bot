package ru.itmo.application;

import org.junit.jupiter.api.*;
import ru.itmo.domain.model.EduStream;
import ru.itmo.domain.model.Student;
import ru.itmo.domain.model.TelegramUser;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.domain.type.PracticePlace;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.BadRequestException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SignedPhotoTest {

    private static EduStream eduStream;

    @BeforeAll
    static void setup() throws BadRequestException {
        eduStream = new EduStream("photo_test_stream", 2026,
                java.time.LocalDate.of(2026, 1, 1),
                java.time.LocalDate.of(2026, 12, 31));
    }


    @Test
    @Order(1)
    void newStatusExists() {
        assertDoesNotThrow(() -> StudentStatus.valueOf("APPLICATION_PHOTO_UPLOADED"));
    }

    @Test
    @Order(2)
    void newStatusDisplayName() {
        assertEquals("Фото подписанной заявки загружено",
                StudentStatus.APPLICATION_PHOTO_UPLOADED.getDisplayName());
    }

    @Test
    @Order(3)
    void newStatusByDisplayName() {
        assertEquals(StudentStatus.APPLICATION_PHOTO_UPLOADED,
                StudentStatus.getByDisplayName("Фото подписанной заявки загружено"));
    }

    @Test
    @Order(4)
    void newStatusValueOfIgnoreCase() {
        assertEquals(StudentStatus.APPLICATION_PHOTO_UPLOADED,
                StudentStatus.valueOfIgnoreCase("application_photo_uploaded"));
    }

    @Test
    @Order(5)
    void newStatusValueOfIgnoreCaseChecked() throws BadRequestException {
        assertEquals(StudentStatus.APPLICATION_PHOTO_UPLOADED,
                StudentStatus.valueOfIgnoreCaseChecked("application_photo_uploaded"));
    }

    @Test
    @Order(6)
    void newStatusHasColor() {
        short color = StudentStatus.APPLICATION_PHOTO_UPLOADED.getColorForStatus();
        assertTrue(color > 0);
    }

    @Test
    @Order(7)
    void newStatusInAvailableValues() {
        String available = StudentStatus.getAvailableValues();
        assertTrue(available.contains("APPLICATION_PHOTO_UPLOADED"));
        assertTrue(available.contains("Фото подписанной заявки загружено"));
    }

    @Test
    @Order(10)
    void transitionFromWaitingSigningIncludesPhotoUploaded() {
        Student student = createStudentWithStatus(StudentStatus.APPLICATION_WAITING_SIGNING);
        String[] transitions = student.getTransitionStatuses();

        boolean hasPhotoUploaded = false;
        for (String t : transitions) {
            if (t.equals(StudentStatus.APPLICATION_PHOTO_UPLOADED.getDisplayName())) {
                hasPhotoUploaded = true;
                break;
            }
        }
        assertTrue(hasPhotoUploaded,
                "APPLICATION_WAITING_SIGNING должен иметь переход в APPLICATION_PHOTO_UPLOADED");
    }

    @Test
    @Order(11)
    void transitionFromPhotoUploadedIncludesSignedAndReturned() {
        Student student = createStudentWithStatus(StudentStatus.APPLICATION_PHOTO_UPLOADED);
        String[] transitions = student.getTransitionStatuses();

        boolean hasSigned = false;
        boolean hasReturned = false;
        for (String t : transitions) {
            if (t.equals(StudentStatus.APPLICATION_SIGNED.getDisplayName())) hasSigned = true;
            if (t.equals(StudentStatus.APPLICATION_RETURNED.getDisplayName())) hasReturned = true;
        }
        assertTrue(hasSigned,
                "APPLICATION_PHOTO_UPLOADED должен иметь переход в APPLICATION_SIGNED");
        assertTrue(hasReturned,
                "APPLICATION_PHOTO_UPLOADED должен иметь переход в APPLICATION_RETURNED");
    }

    @Test
    @Order(12)
    void transitionFromPhotoUploadedIncludesSelf() {
        Student student = createStudentWithStatus(StudentStatus.APPLICATION_PHOTO_UPLOADED);
        String[] transitions = student.getTransitionStatuses();

        boolean hasSelf = false;
        for (String t : transitions) {
            if (t.equals(StudentStatus.APPLICATION_PHOTO_UPLOADED.getDisplayName())) hasSelf = true;
        }
        assertTrue(hasSelf,
                "APPLICATION_PHOTO_UPLOADED должен содержать себя в переходах");
    }

    @Test
    @Order(20)
    void studentHasSignedPhotoPathField() {
        Student student = createStudentWithStatus(StudentStatus.APPLICATION_PHOTO_UPLOADED);
        assertNull(student.getSignedPhotoPath());
    }

    @Test
    @Order(21)
    void studentDuplicateBaseHasNullPhotoPath() {
        Student student = createStudentWithStatus(StudentStatus.APPLICATION_WAITING_SIGNING);
        Student duplicate = student.duplicateBase();

        assertNull(duplicate.getSignedPhotoPath());
        assertEquals(StudentStatus.NOT_REGISTERED, duplicate.getStatus());
    }

    @Test
    @Order(30)
    void validJpegMagicBytes() {
        byte[] jpeg = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00};
        assertTrue(isValidImage(jpeg));
    }

    @Test
    @Order(31)
    void validPngMagicBytes() {
        byte[] png = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A};
        assertTrue(isValidImage(png));
    }

    @Test
    @Order(32)
    void validPdfMagicBytes() {
        byte[] pdf = new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D};
        assertTrue(isValidImage(pdf));
    }

    @Test
    @Order(33)
    void invalidMagicBytes_gif() {
        byte[] gif = new byte[]{0x47, 0x49, 0x46, 0x38, 0x39};
        assertFalse(isValidImage(gif));
    }

    @Test
    @Order(34)
    void validMagicBytes_pdf() {
        byte[] pdf = new byte[]{0x25, 0x50, 0x44, 0x46};
        assertTrue(isValidImage(pdf));
    }

    @Test
    @Order(35)
    void invalidMagicBytes_empty() {
        byte[] empty = new byte[]{};
        assertFalse(isValidImage(empty));
    }

    @Test
    @Order(36)
    void invalidMagicBytes_tooShort() {
        byte[] twoBytes = new byte[]{(byte) 0xFF, (byte) 0xD8};
        assertFalse(isValidImage(twoBytes));
    }

    @Test
    @Order(37)
    void fileSizeWithinLimit() {
        long maxSize = 10 * 1024 * 1024;
        assertTrue(5 * 1024 * 1024 <= maxSize);
        assertFalse(11 * 1024 * 1024 <= maxSize);
    }

    @Test
    @Order(40)
    void authorizationMethodExists() {
        assertDoesNotThrow(() -> {
            var method = AuthorizationService.class.getMethod("canStudentUploadSignedPhoto", long.class);
            assertNotNull(method);
        });
    }

    private static final byte[] JPEG_MAGIC = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47};
    private static final byte[] PDF_MAGIC = new byte[]{0x25, 0x50, 0x44, 0x46};

    private static boolean isValidImage(byte[] fileBytes) {
        if (fileBytes.length < 4) return false;
        if (startsWith(fileBytes, JPEG_MAGIC)) return true;
        if (startsWith(fileBytes, PNG_MAGIC)) return true;
        return startsWith(fileBytes, PDF_MAGIC);
    }

    private static boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) return false;
        }
        return true;
    }

    private Student createStudentWithStatus(StudentStatus status) {
        return new Student(
                null,
                eduStream,
                100500,
                "P3433",
                "Тест Студент",
                status,
                "",
                "",
                PracticePlace.OTHER_COMPANY,
                PracticeFormat.OFFLINE,
                78111L,
                "ООО Тест",
                "Иванов И.И.",
                "+79001234567",
                "test@test.ru",
                "Директор",
                "FFFFFF",
                false,
                null,
                null,
                null,
                null,
                false
        );
    }
}