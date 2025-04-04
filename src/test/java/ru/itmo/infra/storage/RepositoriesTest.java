//package ru.itmo.infra.storage;
//
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import ru.itmo.domain.model.EduStream;
//import ru.itmo.domain.model.Student;
//import ru.itmo.domain.model.TelegramUser;
//import ru.itmo.domain.type.PracticeFormat;
//import ru.itmo.domain.type.PracticePlace;
//import ru.itmo.domain.type.StudentStatus;
//import ru.itmo.exception.InternalException;
//
//import java.time.LocalDate;
//import java.util.List;
//
//public class RepositoriesTest {
//
//    private static final EduStream eduStream = new EduStream(
//            1,
//            "stream",
//            2025,
//            LocalDate.of(2024, 1, 1),
//            LocalDate.of(2025, 1, 1));
//
//    private static final List<TelegramUser> telegramUsers = List.of(
//            new TelegramUser(
//                    1,
//                    false,
//                    false,
//                    "username 1"),
//            new TelegramUser(
//                    2,
//                    false,
//                    false,
//                    "username 2"),
//            new TelegramUser(
//                    3,
//                    false,
//                    false,
//                    "username 3")
//    );
//
//    private static final List<Student> students = List.of(
//            new Student(
//                    telegramUsers.get(0),
//                    eduStream,
//                    1,
//                    "G1",
//                    "name 1",
//                    StudentStatus.REGISTERED,
//                    "comments 1",
//                    "call status comments 1",
//                    PracticePlace.NOT_SPECIFIED,
//                    PracticeFormat.NOT_SPECIFIED,
//                    111,
//                    "company name 1",
//                    "company lead full name 1",
//                    "phone 1",
//                    "email 1",
//                    "manager 1",
//                    "1",
//                    false
//            ),
//            new Student(
//                    telegramUsers.get(1),
//                    eduStream,
//                    2,
//                    "G1",
//                    "name 2",
//                    StudentStatus.APPLICATION_RETURNED,
//                    "comments 2",
//                    "call status comments 2",
//                    PracticePlace.ITMO_UNIVERSITY,
//                    PracticeFormat.ONLINE,
//                    222,
//                    "company name 2",
//                    "company lead full name 2",
//                    "phone 2",
//                    "email 2",
//                    "manager 2",
//                    "2",
//                    false
//            ),
//            new Student(
//                    telegramUsers.get(2),
//                    eduStream,
//                    3,
//                    "G2",
//                    "name 3",
//                    StudentStatus.APPLICATION_RETURNED,
//                    "comments 3",
//                    "call status comments 3",
//                    PracticePlace.ITMO_MARKINA,
//                    PracticeFormat.OFFLINE,
//                    333,
//                    "company name 3",
//                    "company lead full name 3",
//                    "phone 3",
//                    "email 3",
//                    "manager 3",
//                    "3",
//                    false
//            )
//    );
//
//    @BeforeAll
//    static void setup() {
//        try {
//            long id = EduStreamRepository.save(eduStream);
//            eduStream.setId(id);
//            for (var telegramUser : telegramUsers)
//                TelegramUserRepository.save(telegramUser);
//            StudentRepository.saveBatch(students);
//        } catch (InternalException ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    @Test
//    void findAllTest_ok() throws InternalException {
//        Assertions.assertEquals(List.of(eduStream), EduStreamRepository.findAll());
//        Assertions.assertEquals(telegramUsers, TelegramUserRepository.findAll());
//        Assertions.assertEquals(students, StudentRepository.findAll());
//    }
//
//    @Test
//    void filterTest_ok() throws InternalException {
//        Filter filter = Filter.builder()
//                .eduStreamId(eduStream.getId() + 1)
//                .build();
//
//        List<Student> studentsRes = StudentRepository.findAll(filter);
//        Assertions.assertTrue(studentsRes.isEmpty());
//
//
//        filter = Filter.builder()
//                .eduStreamId(eduStream.getId())
//                .stGroups(List.of("G1", "G2"))
//                .build();
//
//        studentsRes = StudentRepository.findAll(filter);
//        Assertions.assertEquals(students, studentsRes);
//
//
//        filter = Filter.builder()
//                .eduStreamId(eduStream.getId())
//                .stGroups(List.of("G1"))
//                .stStatuses(List.of(StudentStatus.APPLICATION_RETURNED))
//                .build();
//
//        studentsRes = StudentRepository.findAll(filter);
//        Assertions.assertEquals(List.of(students.get(1)), studentsRes);
//
//
//        filter = Filter.builder()
//                .stGroups(List.of("G1"))
//                .stStatuses(List.of(StudentStatus.APPLICATION_RETURNED))
//                .build();
//
//        studentsRes = StudentRepository.findAll(filter);
//        Assertions.assertEquals(List.of(students.get(1)), studentsRes);
//    }
//
//    @Test
//    void updateTest_ok() throws InternalException {
//        students.forEach(s -> s.setStGroup("G99"));
//        StudentRepository.updateBatchByChatIdAndEduStreamId(students);
//        Assertions.assertEquals(students, StudentRepository.findAll());
//    }
//
//    @AfterAll
//    static void teardown() {
//        try {
//            EduStreamRepository.deleteById(eduStream.getId());
//            for (var telegramUser : telegramUsers)
//                TelegramUserRepository.deleteByChatId(telegramUser.getChatId());
//            for (var student : students)
//                StudentRepository.deleteByChatIdAndEduStreamId(student.getTelegramUser().getChatId(), student.getEduStream().getId());
//        } catch (InternalException ex) {
//            ex.printStackTrace();
//        }
//    }
//}
