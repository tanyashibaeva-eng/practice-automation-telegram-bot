package ru.itmo.infra.storage;

import org.junit.jupiter.api.*;
import ru.itmo.application.TelegramUserService;
import ru.itmo.domain.dto.ExcelStudentDTO;
import ru.itmo.domain.dto.command.UserRegistrationArgs;
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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RepositoriesTest {

    private static final EduStream eduStream;

    static {
        try {
            eduStream = new EduStream(
                    "stream 1",
                    2025,
                    LocalDate.of(2020, 1, 1),
                    LocalDate.of(2021, 1, 1));
        } catch (BadRequestException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final List<TelegramUser> telegramUsers = List.of(
            new TelegramUser(
                    1,
                    false,
                    false,
                    "username 1"),
            new TelegramUser(
                    2,
                    false,
                    false,
                    "username 2"),
            new TelegramUser(
                    3,
                    false,
                    false,
                    "username 3")
    );

    private static List<Student> students = List.of(
            new Student(
                    null,
                    eduStream,
                    1,
                    "G1",
                    "name 1",
                    StudentStatus.NOT_REGISTERED,
                    "comments 1",
                    "call status comments 1",
                    PracticePlace.NOT_SPECIFIED,
                    PracticeFormat.NOT_SPECIFIED,
                    78111L,
                    "company name 1",
                    "company lead full name 1",
                    "+7 phone 1",
                    "email 1",
                    "manager 1",
                    "1",
                    false,
                    null
            ),
            new Student(
                    null,
                    eduStream,
                    2,
                    "G1",
                    "name 2",
                    StudentStatus.NOT_REGISTERED,
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
                    new byte[]{123, 98, 123, 0, 22}
            ),
            new Student(
                    null,
                    eduStream,
                    3,
                    "G2",
                    "name 3",
                    StudentStatus.NOT_REGISTERED,
                    "comments 3",
                    "call status comments 3",
                    PracticePlace.OTHER_COMPANY,
                    PracticeFormat.OFFLINE,
                    78333L,
                    "company name 3",
                    "company lead full name 3",
                    "+7 phone 3",
                    "email 3",
                    "manager 3",
                    "3",
                    false,
                    new byte[]{0x14}
            )
    );

    @BeforeAll
    static void setup() {
        try {
            EduStreamRepository.save(eduStream);
            RepositoriesTest.saveBatch(students);
        } catch (InternalException | SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Order(1)
    @Test
    void findAllEduStreamAndStudentsBeforeRegistrationTest_ok() throws InternalException {
        Assertions.assertEquals(List.of(eduStream), EduStreamRepository.findAll());
        Assertions.assertEquals(students, StudentRepository.findAll());
    }

    @Order(2)
    @Test
    void registerUsersTest_ok() throws InternalException {
        Assertions.assertDoesNotThrow(() -> {
            for (int i = 0; i < students.size(); i++) {
                TelegramUserService.registerUser(
                        new UserRegistrationArgs(
                                telegramUsers.get(i).getChatId(),
                                telegramUsers.get(i).getUsername(),
                                students.get(i).getEduStream().getName(),
                                students.get(i).getIsu()
                        )
                );
            }
        });

        List<Long> expectedChatIds = telegramUsers.stream().map(TelegramUser::getChatId).toList();

        Assertions.assertEquals(expectedChatIds, StudentRepository.findAll().stream().map(student -> student.getTelegramUser().getChatId()).toList());
    }

    @Order(3)
    @Test
    void filterTest_ok() throws InternalException, BadRequestException {
        students = StudentRepository.findAll();

        Filter filter = Filter.builder()
                .eduStream(new EduStream("NONEXISTENT"))
                .build();

        List<Student> studentsRes = StudentRepository.findAll(filter);
        Assertions.assertTrue(studentsRes.isEmpty());


        filter = Filter.builder()
                .eduStream(eduStream)
                .stGroups(List.of("G1", "G2"))
                .build();

        studentsRes = StudentRepository.findAll(filter);
        Assertions.assertEquals(students, studentsRes);


        filter = Filter.builder()
                .eduStream(eduStream)
                .stGroups(List.of("G1"))
                .stStatuses(List.of(StudentStatus.REGISTERED)) // TODO: test with other status
                .build();

        studentsRes = StudentRepository.findAll(filter);
        Assertions.assertEquals(List.of(students.get(0), students.get(1)), studentsRes);


        filter = Filter.builder()
                .stGroups(List.of("G2"))
                .stStatuses(List.of(StudentStatus.REGISTERED)) // TODO: test with other status
                .build();

        studentsRes = StudentRepository.findAll(filter);
        Assertions.assertEquals(List.of(students.get(2)), studentsRes);
    }

    @Order(4)
    @Test
    void findStudentByIsuAndStreamNameTest_ok() throws InternalException {
        Student st = students.get(0);
        Assertions.assertEquals(st, StudentRepository.findAllByIsuAndEduStreamName(st.getIsu(), st.getEduStream()).get(0));

        Assertions.assertTrue(StudentRepository.findAllByIsuAndEduStreamName(-123, st.getEduStream()).isEmpty());
    }

    @Order(5)
    @Test
    void updateTest_ok() throws InternalException {
        List<ExcelStudentDTO> dtoList = students.stream().map(student -> new ExcelStudentDTO(
                123L,
                student.getIsu(),
                "G99",
                student.getFullName(),
                student.getStatus(),
                student.getComments(),
                student.getCallStatusComments(),
                student.getPracticePlace(),
                student.getPracticeFormat(),
                student.getCompanyINN(),
                student.getCompanyName(),
                student.getCompanyLeadFullName(),
                student.getCompanyLeadPhone(),
                student.getCompanyLeadEmail(),
                student.getCompanyLeadJobTitle(),
                student.getCellHexColor(),
                0
        )).toList();

        List<String> errors;
        for (var i = 0; i < dtoList.size(); i++) {
            errors = students.get(i).updateOrGetErrors(dtoList.get(i));
            Assertions.assertTrue(errors.isEmpty());
        }

        StudentRepository.updateBatchByChatIdAndEduStreamName(students);
        Assertions.assertEquals(students, StudentRepository.findAll());
    }

    @Order(6)
    @Test
    void findAllEduStreamNamesTest_ok() throws InternalException, BadRequestException {
        EduStream es = new EduStream(
                "stream 2",
                2023,
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2024, 1, 1));
        EduStreamRepository.save(es);

        Assertions.assertEquals(List.of("stream 2", "stream 1"), EduStreamRepository.findAllNames());
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

    public static void saveBatch(List<Student> students) throws InternalException, SQLException {
        final Connection connection = DatabaseManager.getConnection();
        try (var statement = connection.prepareStatement("""
                    INSERT INTO student (
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
                        managed_manually,
                        application_bytes
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                """
        )) {
            for (var student : students) {
                statement.setString(1, student.getEduStream().getName());
                statement.setInt(2, student.getIsu());
                statement.setString(3, student.getStGroup());
                statement.setString(4, student.getFullName());
                statement.setObject(5, student.getStatus(), Types.OTHER);
                statement.setString(6, student.getComments());
                statement.setString(7, student.getCallStatusComments());
                statement.setObject(8, student.getPracticePlace(), Types.OTHER);
                statement.setObject(9, student.getPracticeFormat(), Types.OTHER);

                Long companyINN = student.getCompanyINN();
                if (companyINN == null) {
                    statement.setNull(10, Types.BIGINT);
                } else statement.setLong(10, student.getCompanyINN());

                statement.setString(11, student.getCompanyName());
                statement.setString(12, student.getCompanyLeadFullName());
                statement.setString(13, student.getCompanyLeadPhone());
                statement.setString(14, student.getCompanyLeadEmail());
                statement.setString(15, student.getCompanyLeadJobTitle());
                statement.setString(16, student.getCellHexColor());
                statement.setBoolean(17, student.isManagedManually());
                statement.setBytes(18, student.getApplicationBytes());
                statement.addBatch();
            }
            statement.executeBatch();

        }
    }

}