package ru.itmo.infra.storage;

import lombok.extern.java.Log;
import ru.itmo.domain.dto.command.CompanyInfoUpdateArgs;
import ru.itmo.domain.dto.command.ITMOPracticeInfoUpdateArgs;
import ru.itmo.domain.model.EduStream;
import ru.itmo.domain.model.Student;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.domain.type.PracticePlace;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

@Log
public class StudentRepository {

    private static final Connection connection = DatabaseManager.getConnection();

    public static void saveBaseBatch(List<Student> students) throws InternalException, BadRequestException {
        try (var statement = connection.prepareStatement("""
                    INSERT INTO student (
                        edu_stream_name,
                        isu,
                        st_group,
                        fullname
                    ) VALUES (?, ?, ?, ?);
                """
        )) {
            for (var student : students) {
                statement.setString(1, student.getEduStream().getName());
                statement.setInt(2, student.getIsu());
                statement.setString(3, student.getStGroup());
                statement.setString(4, student.getFullName());
                statement.addBatch();
            }
            statement.executeBatch();

        } catch (SQLException ex) {
            /* violates index "idx_isu_edu_stream_name_student" constraint */
            if (ex.getSQLState().equals("23505")) {
                throw new BadRequestException("Некоторые из переданных в файле студентов уже существуют в потоке, загрузите новый файл. При переходе обратно в меню все загруженные на данный момент файлы уже сохранены");
            }
            throw handleAndWrapSQLException(ex);
        }
    }

    public static void saveTransactional(Student student, Connection transactionConnection) throws SQLException {
        try (var statement = transactionConnection.prepareStatement("""
                    INSERT INTO student (
                        edu_stream_name,
                        isu,
                        st_group,
                        fullname
                    ) VALUES (?, ?, ?, ?);
                """
        )) {
            statement.setString(1, student.getEduStream().getName());
            statement.setInt(2, student.getIsu());
            statement.setString(3, student.getStGroup());
            statement.setString(4, student.getFullName());
            statement.executeUpdate();
        }
    }

    public static void updateChatIdTransactional(Student student, Connection transactionConnection) throws SQLException {
        try (var statement = transactionConnection.prepareStatement("""
                UPDATE student
                SET chat_id = ?, status = 'REGISTERED'
                WHERE edu_stream_name = ?
                    AND isu = ?
                    AND st_group = ?
                    AND fullname = ?
                    AND chat_id IS NULL;
                """
        )) {
            statement.setLong(1, student.getTelegramUser().getChatId());
            statement.setString(2, student.getEduStream().getName());
            statement.setInt(3, student.getIsu());
            statement.setString(4, student.getStGroup());
            statement.setString(5, student.getFullName());
            statement.executeUpdate();
        }
    }

    public static List<Student> findAll() throws InternalException {
        try (var statement = connection.prepareStatement(
                "SELECT * FROM student;"
        )) {
            var rs = statement.executeQuery();
            return mapToStudentList(rs);

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static List<Student> findAll(Filter filter) throws InternalException {
        if (filter.isEmpty())
            return findAll();

        String query = buildFilteringQuery(filter);

        log.info("built query with filters: " + query);

        try (var statement = connection.prepareStatement(query)) {
            var rs = statement.executeQuery();
            List<Student> result = mapToStudentList(rs);
            result.sort(Comparator.comparing(Student::getFullName));
            return result;

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    private static String buildFilteringQuery(Filter filter) {
        String query = "SELECT * FROM student WHERE ";
        StringJoiner stringJoiner = new StringJoiner(" AND ");

        EduStream eduStream = filter.getEduStream();
        if (eduStream != null)
            stringJoiner.add("edu_stream_name = '%s'".formatted(eduStream.getName()));

        List<String> stGroups = filter.getStGroups();
        if (stGroups != null && !stGroups.isEmpty()) {
            StringJoiner sj = new StringJoiner(" OR ");
            for (var stGroup : stGroups)
                sj.add("st_group = '%s'".formatted(stGroup));
            stringJoiner.add("(" + sj + ")");
        }

        List<StudentStatus> stStatuses = filter.getStStatuses();
        if (stStatuses != null && !stStatuses.isEmpty()) {
            StringJoiner sj = new StringJoiner(" OR ");
            for (var stStatus : stStatuses)
                sj.add("status = '%s'".formatted(stStatus.name().toUpperCase()));
            stringJoiner.add("(" + sj + ")");
        }

        return query + stringJoiner + ";";
    }

    public static boolean existsByChatIdAndEduStreamName(long chatId, EduStream eduStream) throws InternalException {
        try (var statement = connection.prepareStatement(
                "SELECT * FROM student WHERE chat_id = ? AND edu_stream_name = ?;"
        )) {
            statement.setLong(1, chatId);
            statement.setString(2, eduStream.getName());
            var rs = statement.executeQuery();
            return rs.next();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static List<Student> findAllByChatId(long chatId) throws InternalException {
        try (var statement = connection.prepareStatement(
                "SELECT * FROM student WHERE chat_id = ?;"
        )) {
            statement.setLong(1, chatId);
            var rs = statement.executeQuery();
            return mapToStudentList(rs);

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static Optional<Student> findByChatIdAndEduStreamName(long chatId, EduStream eduStream) throws InternalException {
        try (var statement = connection.prepareStatement(
                "SELECT * FROM student WHERE chat_id = ? AND edu_stream_name = ?;"
        )) {
            statement.setLong(1, chatId);
            statement.setString(2, eduStream.getName());
            var rs = statement.executeQuery();
            return mapToStudentOptional(rs);

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static List<Student> findAllByIsuAndEduStreamName(int isu, EduStream eduStream) throws InternalException {
        try (var statement = connection.prepareStatement(
                "SELECT * FROM student WHERE isu = ? AND edu_stream_name = ?;"
        )) {
            statement.setInt(1, isu);
            statement.setString(2, eduStream.getName());
            var rs = statement.executeQuery();

            List<Student> result = mapToStudentList(rs);
            result.sort(Comparator.comparing(Student::getFullName));
            return result;

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean deleteByChatId(long chatId) throws InternalException {
        try (var statement = connection.prepareStatement(
                "DELETE FROM student WHERE chat_id = ?;"
        )) {
            statement.setLong(1, chatId);
            return 1 == statement.executeUpdate();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean updateCompanyInfo(CompanyInfoUpdateArgs args, String eduStreamName) throws InternalException {
        try (var statement = connection.prepareStatement("""
                    UPDATE student SET
                        status = ?,
                        practice_place = ?,
                        practice_format = ?,
                        company_inn = ?,
                        company_name = ?,
                        company_lead_fullname = ?,
                        company_lead_phone = ?,
                        company_lead_email = ?,
                        company_lead_job_title = ?,
                        updated_at = now()
                    WHERE chat_id = ? AND edu_stream_name = ?;
                """
        )) {
            statement.setObject(1, StudentStatus.COMPANY_INFO_WAITING_APPROVAL, Types.OTHER);
            statement.setObject(2, PracticePlace.OTHER_COMPANY, Types.OTHER);
            statement.setObject(3, args.getPracticeFormat(), Types.OTHER);
            statement.setLong(4, args.getInn());
            statement.setString(5, args.getCompanyName());
            statement.setString(6, args.getCompanyLeadFullName());
            statement.setString(7, args.getCompanyLeadPhone());
            statement.setString(8, args.getCompanyLeadEmail());
            statement.setString(9, args.getCompanyLeadJobTitle());
            statement.setLong(10, args.getChatId());
            statement.setString(11, eduStreamName);
            return 1 == statement.executeUpdate();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean updateITMOPracticeInfo(ITMOPracticeInfoUpdateArgs args, String eduStreamName) throws InternalException {
        try (var statement = connection.prepareStatement("""
                    UPDATE student SET
                        status = ?,
                        practice_place = ?,
                        company_name = ?,
                        company_lead_fullname = ?,
                        updated_at = now()
                    WHERE chat_id = ? AND edu_stream_name = ?;
                """
        )) {
            statement.setObject(1, (args.getPracticePlace() == PracticePlace.ITMO_MARKINA ? StudentStatus.PRACTICE_IN_ITMO_MARKINA : StudentStatus.COMPANY_INFO_WAITING_APPROVAL), Types.OTHER);
            statement.setObject(2, args.getPracticePlace(), Types.OTHER);
            statement.setString(3, args.getCompanyName());
            statement.setString(4, args.getCompanyLeadFullName());
            statement.setLong(5, args.getChatId());
            statement.setString(6, eduStreamName);
            return 1 == statement.executeUpdate();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static List<Student> exportAll(EduStream eduStream) throws InternalException {
        try (var statement = connection.prepareStatement("""
                UPDATE student
                SET exported_at = now()
                WHERE edu_stream_name = ?
                RETURNING *;
                """
        )) {
            statement.setString(1, eduStream.getName());
            var rs = statement.executeQuery();
            List<Student> result = mapToStudentList(rs);
            result.sort(Comparator.comparing(Student::getFullName));
            return result;

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static int[] updateBatchByChatIdAndEduStreamName(List<Student> students) throws InternalException {
        try (var statement = connection.prepareStatement("""
                    UPDATE student SET
                        isu = ?,
                        st_group = ?,
                        fullname = ?,
                        status = ?,
                        comments = ?,
                        call_status_comments = ?,
                        practice_place = ?,
                        practice_format = ?,
                        company_inn = ?,
                        company_name = ?,
                        company_lead_fullname = ?,
                        company_lead_phone = ?,
                        company_lead_email = ?,
                        company_lead_job_title = ?,
                        cell_hex_color = ?,
                        managed_manually = ?,
                        application_bytes = ?,
                        signed_photo_path = ?,
                        updated_at = now()
                    WHERE (chat_id = ? OR (chat_id IS NULL AND ? IS NULL)) AND isu = ? AND edu_stream_name = ?;
                """
        )) {
            var updated = new ArrayList<Integer>();
            for (var student : students) {
                statement.setInt(1, student.getIsu());
                statement.setString(2, student.getStGroup());
                statement.setString(3, student.getFullName());
                statement.setObject(4, student.getStatus(), Types.OTHER);
                statement.setString(5, student.getComments() == null ? "" : student.getComments());
                statement.setString(6, student.getCallStatusComments() == null ? "" : student.getCallStatusComments());
                statement.setObject(7, student.getPracticePlace(), Types.OTHER);
                statement.setObject(8, student.getPracticeFormat(), Types.OTHER);

                Long companyINN = student.getCompanyINN();
                if (companyINN == null) {
                    statement.setNull(9, Types.INTEGER);
                } else statement.setLong(9, student.getCompanyINN());

                statement.setString(10, student.getCompanyName());
                statement.setString(11, student.getCompanyLeadFullName());
                statement.setString(12, student.getCompanyLeadPhone());
                statement.setString(13, student.getCompanyLeadEmail());
                statement.setString(14, student.getCompanyLeadJobTitle());
                statement.setString(15, student.getCellHexColor().equals("000000") ? "FFFFFF" : student.getCellHexColor());
                statement.setBoolean(16, student.isManagedManually());
                statement.setBytes(17, student.getApplicationBytes());
                statement.setString(18, student.getSignedPhotoPath());

                if (student.getTelegramUser() != null) {
                    statement.setLong(19, student.getTelegramUser().getChatId());
                    statement.setLong(20, student.getTelegramUser().getChatId()); // второй параметр для проверки на
                    // NULL
                } else {
                    statement.setNull(19, Types.BIGINT);
                    statement.setNull(20, Types.BIGINT); // второй параметр для проверки на NULL
                }

                statement.setInt(21, student.getIsu());
                statement.setString(22, student.getEduStream().getName());

                updated.add(statement.executeUpdate());
            }

            return updated.stream().mapToInt(i -> i).toArray();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean updateSignedPhotoPath(long chatId, String eduStreamName, String photoPath) throws InternalException {
        try (var statement = connection.prepareStatement(
                "UPDATE student SET signed_photo_path = ?, status = ?, updated_at = now() WHERE chat_id = ? AND edu_stream_name = ?;"
        )) {
            statement.setString(1, photoPath);
            statement.setObject(2, StudentStatus.APPLICATION_PHOTO_UPLOADED, Types.OTHER);
            statement.setLong(3, chatId);
            statement.setString(4, eduStreamName);
            return 1 == statement.executeUpdate();
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean updateApplicationBytesByChatIdAndEduStreamName(long chatId, String eduStreamName, byte[] newBytes) throws InternalException {
        try (var statement = connection.prepareStatement(
                "UPDATE student SET application_bytes = ?, status = ? WHERE chat_id = ? AND edu_stream_name = ?;"
        )) {
            statement.setBytes(1, newBytes);
            statement.setObject(2, StudentStatus.APPLICATION_WAITING_APPROVAL, Types.OTHER);
            statement.setLong(3, chatId);
            statement.setString(4, eduStreamName);
            return 1 == statement.executeUpdate();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    private static Optional<Student> mapToStudentOptional(ResultSet rs) throws SQLException, InternalException {
        Student student = mapToStudent(rs);
        if (student == null) return Optional.empty();
        return Optional.of(student);
    }

    private static List<Student> mapToStudentList(ResultSet rs) throws SQLException, InternalException {
        List<Student> result = new ArrayList<>();

        Student student = mapToStudent(rs);
        while (student != null) {
            result.add(student);
            student = mapToStudent(rs);
        }

        return result;
    }

    private static Student mapToStudent(ResultSet rs) throws SQLException, InternalException {
        if (rs.next()) {
            long chatId = rs.getLong("chat_id");

            EduStream eduStream;

            try {
                eduStream = new EduStream(rs.getString("edu_stream_name"));
                eduStream = EduStreamRepository.findByName(eduStream).orElseThrow(
                        () -> new InternalException("Ошибка чтения данных из базы: нарушена консистентность соотношения студентов и учебных потоков"));
            } catch (BadRequestException ex) {
                throw new InternalException("Ошибка чтения данных из базы: нарушена консистентность соотношения студентов и учебных потоков");
            }

            return new Student(
                    (chatId == 0)
                            ? null
                            : TelegramUserRepository.findByChatId(rs.getLong("chat_id"))
                            .orElseThrow(() -> new InternalException("Ошибка чтения данных из базы: нарушена консистентность соотношения студентов и телеграм-пользователей")),
                    eduStream,
                    rs.getInt("isu"),
                    rs.getString("st_group"),
                    rs.getString("fullname"),
                    StudentStatus.valueOfIgnoreCase(rs.getString("status")),
                    rs.getString("comments"),
                    rs.getString("call_status_comments"),
                    PracticePlace.valueOfIgnoreCase(rs.getString("practice_place")),
                    PracticeFormat.valueOfIgnoreCase(rs.getString("practice_format")),
                    rs.getLong("company_inn") == 0 ? null : rs.getLong("company_inn"),
                    rs.getString("company_name"),
                    rs.getString("company_lead_fullname"),
                    rs.getString("company_lead_phone"),
                    rs.getString("company_lead_email"),
                    rs.getString("company_lead_job_title"),
                    rs.getString("cell_hex_color"),
                    rs.getBoolean("managed_manually"),
                    rs.getTimestamp("exported_at"),
                    rs.getTimestamp("updated_at"),
                    rs.getBytes("application_bytes"),
                    rs.getString("signed_photo_path"),
                    false
            );
        }
        return null;
    }

    private static InternalException handleAndWrapSQLException(SQLException ex) {
        log.severe("Ошибка во время выполнения SQL запроса: " + ex.getMessage());
        return new InternalException("Что-то пошло не так", ex.getCause());
    }
}
