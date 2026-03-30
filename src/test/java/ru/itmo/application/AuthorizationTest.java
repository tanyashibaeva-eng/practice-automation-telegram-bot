package ru.itmo.application;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
import ru.itmo.infra.storage.TelegramUserRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;

public class AuthorizationTest {

    private static final TelegramUser adminUser = new TelegramUser(1, true, false, "admin");
    private static final TelegramUser bannedAdminUser = new TelegramUser(2, true, true, "banned admin");
    private static final TelegramUser bannedStudentUser = new TelegramUser(3, false, true, "banned student");
    private static final TelegramUser newUser = new TelegramUser(4, false, false, "new user");
    private static final TelegramUser studentWithPreviousRegistrationUser = new TelegramUser(5, false, false, "student with previous registration");
    private static final TelegramUser studentWithRegisteredStatusUser = new TelegramUser(6, false, false, "student with registered status");
    private static final TelegramUser studentWithApplicationReturnedStatusUser = new TelegramUser(7, false, false, "student with application returned status");
    private static final TelegramUser studentWithApplicationWaitingSigningStatusUser = new TelegramUser(8, false, false, "student with application waiting signing status");

    private static final EduStream endedEduStream;
    private static final EduStream ongoingEduStream;

    private static final Student newStudent;
    private static final Student studentWithPreviousRegistrationPrevious;
    private static final Student studentWithPreviousRegistrationCurrent;
    private static final Student studentWithRegisteredStatusPrevious;
    private static final Student studentWithRegisteredStatus;
    private static final Student studentWithApplicationReturnedStatus;
    private static final Student studentWithApplicationWaitingSigningStatus;

    static {
        try {
            endedEduStream = new EduStream(
                    "ended",
                    2020,
                    LocalDate.of(2020, 1, 1),
                    LocalDate.of(2021, 1, 1));
            ongoingEduStream = new EduStream(
                    "ongoing",
                    LocalDate.now().getYear(),
                    LocalDate.of(LocalDate.now().getYear(), 1, 1),
                    LocalDate.of(LocalDate.now().getYear() + 1, 1, 1));

            newStudent = new Student(
                    null,
                    ongoingEduStream,
                    444444,
                    "G2",
                    "new student",
                    StudentStatus.NOT_REGISTERED,
                    null,
                    null,
                    PracticePlace.NOT_SPECIFIED,
                    PracticeFormat.NOT_SPECIFIED,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    false,
                    null,
                    null,
                    null,
                    null,
                    false
            );
            studentWithPreviousRegistrationPrevious = new Student(
                    studentWithPreviousRegistrationUser,
                    endedEduStream,
                    555555,
                    "G1",
                    "student with previous registration",
                    StudentStatus.APPLICATION_SIGNED,
                    "something",
                    "123",
                    PracticePlace.OTHER_COMPANY,
                    PracticeFormat.OFFLINE,
                    1234567890L,
                    "COMPANY",
                    "name",
                    "+79999999999",
                    "email@domain.com",
                    "manager",
                    "123123",
                    false,
                    null,
                    null,
                    null,
                    null,
                    false
            );
            studentWithPreviousRegistrationCurrent = new Student(
                    null,
                    ongoingEduStream,
                    555555,
                    "G2",
                    "student with previous registration",
                    StudentStatus.NOT_REGISTERED,
                    null,
                    null,
                    PracticePlace.NOT_SPECIFIED,
                    PracticeFormat.NOT_SPECIFIED,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    false,
                    null,
                    null,
                    null,
                    null,
                    false
            );
            studentWithRegisteredStatusPrevious = new Student(
                    studentWithRegisteredStatusUser,
                    endedEduStream,
                    666666,
                    "G1",
                    "student with registered status previous",
                    StudentStatus.APPLICATION_SIGNED,
                    "something",
                    "123",
                    PracticePlace.OTHER_COMPANY,
                    PracticeFormat.OFFLINE,
                    1234567890L,
                    "COMPANY",
                    "name",
                    "+79999999999",
                    "email@domain.com",
                    "manager",
                    "123123",
                    false,
                    null,
                    null,
                    null,
                    null,
                    false
            );
            studentWithRegisteredStatus = new Student(
                    studentWithRegisteredStatusUser,
                    ongoingEduStream,
                    666666,
                    "G2",
                    "student with registered status",
                    StudentStatus.REGISTERED,
                    null,
                    null,
                    PracticePlace.NOT_SPECIFIED,
                    PracticeFormat.NOT_SPECIFIED,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    false,
                    null,
                    null,
                    null,
                    null,
                    false
            );
            studentWithApplicationReturnedStatus = new Student(
                    studentWithApplicationReturnedStatusUser,
                    ongoingEduStream,
                    777777,
                    "G2",
                    "student with application returned status",
                    StudentStatus.APPLICATION_RETURNED,
                    null,
                    null,
                    PracticePlace.NOT_SPECIFIED,
                    PracticeFormat.NOT_SPECIFIED,
                    9999999999L,
                    "COMPANY 2",
                    "name",
                    "+79999999999",
                    "email@domain.com",
                    "manager",
                    "123123",
                    false,
                    null,
                    null,
                    null,
                    null,
                    false
            );
            studentWithApplicationWaitingSigningStatus = new Student(
                    studentWithApplicationWaitingSigningStatusUser,
                    ongoingEduStream,
                    888888,
                    "G2",
                    "student with application waiting signing status",
                    StudentStatus.APPLICATION_WAITING_SIGNING,
                    null,
                    null,
                    PracticePlace.OTHER_COMPANY,
                    PracticeFormat.ONLINE,
                    9999999999L,
                    "COMPANY 2",
                    "name",
                    "+79999999999",
                    "email@domain.com",
                    "manager",
                    "123123",
                    false,
                    null,
                    null,
                    null,
                    null,
                    false
            );
        } catch (BadRequestException ex) {
            throw new RuntimeException(ex);
        }
    }

    @BeforeAll
    static void setup() throws InternalException, SQLException {
        TelegramUserRepository.save(adminUser);
        TelegramUserRepository.save(bannedAdminUser);
        TelegramUserRepository.save(bannedStudentUser);
        TelegramUserRepository.save(studentWithPreviousRegistrationUser);
        TelegramUserRepository.save(studentWithRegisteredStatusUser);
        TelegramUserRepository.save(studentWithApplicationReturnedStatusUser);
        TelegramUserRepository.save(studentWithApplicationWaitingSigningStatusUser);

        EduStreamRepository.save(endedEduStream);
        EduStreamRepository.save(ongoingEduStream);

        saveBatchForce(List.of(
                newStudent,
                studentWithPreviousRegistrationPrevious,
                studentWithPreviousRegistrationCurrent,
                studentWithRegisteredStatusPrevious,
                studentWithRegisteredStatus,
                studentWithApplicationReturnedStatus,
                studentWithApplicationWaitingSigningStatus
        ));
    }

    @Test
    void canAdminDoAdminActionsTest() throws InternalException {
        Assertions.assertTrue(AuthorizationService.canDoAdminActions(adminUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canDoAdminActions(bannedAdminUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canDoAdminActions(newUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canDoAdminActions(bannedStudentUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canDoAdminActions(studentWithPreviousRegistrationUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canDoAdminActions(studentWithRegisteredStatusUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canDoAdminActions(studentWithApplicationReturnedStatusUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canDoAdminActions(studentWithApplicationWaitingSigningStatusUser.getChatId()));
    }

    @Test
    void canRegisterAsAdminTest() throws InternalException {
        Assertions.assertTrue(AuthorizationService.canRegisterAsAdmin(adminUser.getChatId()));
        Assertions.assertTrue(AuthorizationService.canRegisterAsAdmin(bannedAdminUser.getChatId()));
        Assertions.assertTrue(AuthorizationService.canRegisterAsAdmin(newUser.getChatId()));
        Assertions.assertTrue(AuthorizationService.canRegisterAsAdmin(bannedStudentUser.getChatId()));
        Assertions.assertTrue(AuthorizationService.canRegisterAsAdmin(studentWithPreviousRegistrationUser.getChatId()));
        Assertions.assertTrue(AuthorizationService.canRegisterAsAdmin(studentWithRegisteredStatusUser.getChatId()));
        Assertions.assertTrue(AuthorizationService.canRegisterAsAdmin(studentWithApplicationReturnedStatusUser.getChatId()));
        Assertions.assertTrue(AuthorizationService.canRegisterAsAdmin(studentWithApplicationWaitingSigningStatusUser.getChatId()));
    }

    @Test
    void canRegisterAsStudentTest() throws InternalException {
        Assertions.assertFalse(AuthorizationService.canRegisterAsStudent(adminUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canRegisterAsStudent(bannedAdminUser.getChatId()));
        Assertions.assertTrue(AuthorizationService.canRegisterAsStudent(newUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canRegisterAsStudent(bannedStudentUser.getChatId()));
        Assertions.assertTrue(AuthorizationService.canRegisterAsStudent(studentWithPreviousRegistrationUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canRegisterAsStudent(studentWithRegisteredStatusUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canRegisterAsStudent(studentWithApplicationReturnedStatusUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canRegisterAsStudent(studentWithApplicationWaitingSigningStatusUser.getChatId()));
    }

    @Test
    void canStudentUpdateCompanyInfoTest() throws InternalException {
        Assertions.assertFalse(AuthorizationService.canStudentUpdateCompanyInfo(adminUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canStudentUpdateCompanyInfo(bannedAdminUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canStudentUpdateCompanyInfo(newUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canStudentUpdateCompanyInfo(bannedStudentUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canStudentUpdateCompanyInfo(studentWithPreviousRegistrationUser.getChatId()));
        Assertions.assertTrue(AuthorizationService.canStudentUpdateCompanyInfo(studentWithRegisteredStatusUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canStudentUpdateCompanyInfo(studentWithApplicationReturnedStatusUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canStudentUpdateCompanyInfo(studentWithApplicationWaitingSigningStatusUser.getChatId()));
    }

    @Test
    void canStudentSubmitApplicationTest() throws InternalException {
        Assertions.assertFalse(AuthorizationService.canStudentSubmitApplication(adminUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canStudentSubmitApplication(bannedAdminUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canStudentSubmitApplication(newUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canStudentSubmitApplication(bannedStudentUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canStudentSubmitApplication(studentWithPreviousRegistrationUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canStudentSubmitApplication(studentWithRegisteredStatusUser.getChatId()));
        Assertions.assertTrue(AuthorizationService.canStudentSubmitApplication(studentWithApplicationReturnedStatusUser.getChatId()));
        Assertions.assertTrue(AuthorizationService.canStudentSubmitApplication(studentWithApplicationWaitingSigningStatusUser.getChatId()));
    }

    @Test
    void canStudentDownloadApplicationTest() throws InternalException {
        Assertions.assertFalse(AuthorizationService.canStudentDownloadApplication(adminUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canStudentDownloadApplication(bannedAdminUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canStudentDownloadApplication(newUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canStudentDownloadApplication(bannedStudentUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canStudentDownloadApplication(studentWithPreviousRegistrationUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canStudentDownloadApplication(studentWithRegisteredStatusUser.getChatId()));
        Assertions.assertTrue(AuthorizationService.canStudentDownloadApplication(studentWithApplicationReturnedStatusUser.getChatId()));
        Assertions.assertFalse(AuthorizationService.canStudentDownloadApplication(studentWithApplicationWaitingSigningStatusUser.getChatId()));
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

    private static void saveBatchForce(List<Student> students) throws SQLException {
        final Connection connection = DatabaseManager.getConnection();
        try (var statement = connection.prepareStatement("""
                    INSERT INTO student (
                        chat_id,
                        edu_stream_name,
                        isu,
                        st_group,
                        fullname,
                        status,
                        comments,
                        call_status_comments,
                        practice_place,
                        practice_format,
                        company_inn,
                        company_name,
                        company_lead_fullname,
                        company_lead_phone,
                        company_lead_email,
                        company_lead_job_title,
                        cell_hex_color,
                        managed_manually
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                """
        )) {
            Long chatId;
            String eduStreamName;
            int isu;
            String stGroup;
            String fullName;
            StudentStatus status;
            String comments;
            String callStatusComments;
            PracticePlace practicePlace;
            PracticeFormat practiceFormat;
            Long companyINN;
            String companyName;
            String companyLeadFullName;
            String companyLeadPhone;
            String companyLeadEmail;
            String companyLeadJobTitle;
            String cellHexColor;
            boolean managedManually;

            for (var student : students) {
                chatId = (student.getTelegramUser() == null) ? null : student.getTelegramUser().getChatId();
                eduStreamName = student.getEduStream().getName();
                isu = student.getIsu();
                stGroup = student.getStGroup();
                fullName = student.getFullName();
                status = student.getStatus();
                comments = student.getComments();
                callStatusComments = student.getCallStatusComments();
                practicePlace = student.getPracticePlace();
                practiceFormat = student.getPracticeFormat();
                companyINN = student.getCompanyINN();
                companyName = student.getCompanyName();
                companyLeadFullName = student.getCompanyLeadFullName();
                companyLeadPhone = student.getCompanyLeadPhone();
                companyLeadEmail = student.getCompanyLeadEmail();
                companyLeadJobTitle = student.getCompanyLeadJobTitle();
                cellHexColor = student.getCellHexColor();
                managedManually = student.isManagedManually();

                if (chatId == null)
                    statement.setNull(1, Types.BIGINT);
                else
                    statement.setLong(1, chatId);

                statement.setString(2, eduStreamName);
                statement.setInt(3, isu);
                statement.setString(4, stGroup);
                statement.setString(5, fullName);

                if (status == null)
                    statement.setObject(6, StudentStatus.NOT_REGISTERED, Types.OTHER);
                else
                    statement.setObject(6, status, Types.OTHER);

                if (comments == null)
                    statement.setString(7, "");
                else
                    statement.setString(7, comments);

                if (callStatusComments == null)
                    statement.setString(8, "");
                else
                    statement.setString(8, callStatusComments);

                if (practicePlace == null)
                    statement.setObject(9, PracticePlace.NOT_SPECIFIED, Types.OTHER);
                else
                    statement.setObject(9, practicePlace, Types.OTHER);

                if (practiceFormat == null)
                    statement.setObject(10, PracticeFormat.NOT_SPECIFIED, Types.OTHER);
                else
                    statement.setObject(10, practiceFormat, Types.OTHER);

                if (companyINN == null)
                    statement.setNull(11, Types.BIGINT);
                else
                    statement.setLong(11, companyINN);

                if (companyName == null)
                    statement.setNull(12, Types.VARCHAR);
                else
                    statement.setString(12, companyName);

                if (companyLeadFullName == null)
                    statement.setNull(13, Types.VARCHAR);
                else
                    statement.setString(13, companyLeadFullName);

                if (companyLeadPhone == null)
                    statement.setNull(14, Types.VARCHAR);
                else
                    statement.setString(15, companyLeadPhone);

                if (companyLeadEmail == null)
                    statement.setNull(15, Types.VARCHAR);
                else
                    statement.setString(15, companyLeadEmail);

                if (companyLeadJobTitle == null)
                    statement.setNull(16, Types.VARCHAR);
                else
                    statement.setString(16, companyLeadJobTitle);

                if (cellHexColor == null)
                    statement.setString(17, "FFFFFFF");
                else
                    statement.setString(17, cellHexColor);

                statement.setBoolean(18, managedManually);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

}
