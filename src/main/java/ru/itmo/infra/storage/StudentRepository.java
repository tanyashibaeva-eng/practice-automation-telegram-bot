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

import java.sql.*;
import java.sql.*;
import java.util.*;

@Log
public class StudentRepository {

    public static void saveBaseBatch(List<Student> students) throws InternalException, BadRequestException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
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
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(
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

        var queryAndParams = buildFilteringQuery(filter);
        String query = queryAndParams.query;
        List<Object> params = queryAndParams.params;

        log.info("built query with filters: " + query);

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(query)) {
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }
            var rs = statement.executeQuery();
            List<Student> result = mapToStudentList(rs);
            result.sort(Comparator.comparing(Student::getFullName));
            return result;

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    private record FilterQuery(String query, List<Object> params) {}

    private static FilterQuery buildFilteringQuery(Filter filter) {
        String query = "SELECT * FROM student WHERE ";
        StringJoiner stringJoiner = new StringJoiner(" AND ");
        List<Object> params = new ArrayList<>();

        EduStream eduStream = filter.getEduStream();
        if (eduStream != null) {
            stringJoiner.add("edu_stream_name = ?");
            params.add(eduStream.getName());
        }

        List<String> stGroups = filter.getStGroups();
        if (stGroups != null && !stGroups.isEmpty()) {
            StringJoiner sj = new StringJoiner(" OR ");
            for (var stGroup : stGroups) {
                sj.add("st_group = ?");
                params.add(stGroup);
            }
            stringJoiner.add("(" + sj + ")");
        }

        List<StudentStatus> stStatuses = filter.getStStatuses();
        if (stStatuses != null && !stStatuses.isEmpty()) {
            StringJoiner sj = new StringJoiner(" OR ");
            for (var stStatus : stStatuses) {
                sj.add("status = CAST(? AS st_status)");
                params.add(stStatus.name().toUpperCase());
            }
            stringJoiner.add("(" + sj + ")");
        }

        return new FilterQuery(query + stringJoiner + ";", params);
    }

    public static boolean existsByChatIdAndEduStreamName(long chatId, EduStream eduStream) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(
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
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(
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
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(
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

    public static Optional<Student> findStudentByIsuAndEduStreamName(int isu, EduStream eduStream) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(
                "SELECT * FROM student WHERE isu = ? AND edu_stream_name = ?;"
        )) {
            statement.setLong(1, isu);
            statement.setString(2, eduStream.getName());
            var rs = statement.executeQuery();
            return mapToStudentOptional(rs);
    }
    public static List<Student> findAllByIsu(int isu) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(
                "SELECT * FROM student WHERE isu = ?;"
        )) {
            statement.setInt(1, isu);
            var rs = statement.executeQuery();

            List<Student> result = mapToStudentList(rs);
            result.sort(Comparator.comparing(Student::getFullName));
            return result;
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }


    public static List<Student> findAllByIsuAndEduStreamName(int isu, EduStream eduStream) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(
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
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(
                "DELETE FROM student WHERE chat_id = ?;"
        )) {
            statement.setLong(1, chatId);
            return 1 == statement.executeUpdate();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean deleteByChatIdTransactional(long chatId, Connection transactionConnection)
            throws SQLException {
        try (var statement = transactionConnection.prepareStatement(
                "DELETE FROM student WHERE chat_id = ?;"
        )) {
            statement.setLong(1, chatId);
            return 1 == statement.executeUpdate();
        }
    }

    public static boolean updateCompanyInfo(CompanyInfoUpdateArgs args, String eduStreamName) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                    UPDATE student SET
                        status = ?,
                        practice_place = ?,
                        practice_format = ?,
                        practice_format_id = COALESCE(?, (SELECT id FROM practice_format WHERE lower(code) = lower(?) LIMIT 1)),
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
            if (args.getPracticeFormatId() == null) {
                statement.setNull(4, Types.BIGINT);
            } else {
                statement.setLong(4, args.getPracticeFormatId());
            }
            statement.setString(5, args.getPracticeFormat() == null ? null : args.getPracticeFormat().name());

            statement.setLong(6, args.getInn());
            statement.setString(7, args.getCompanyName());
            statement.setString(8, args.getCompanyLeadFullName());
            statement.setString(9, args.getCompanyLeadPhone());
            statement.setString(10, args.getCompanyLeadEmail());
            statement.setString(11, args.getCompanyLeadJobTitle());
            statement.setLong(12, args.getChatId());
            statement.setString(13, eduStreamName);
            return 1 == statement.executeUpdate();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean updateITMOPracticeInfo(ITMOPracticeInfoUpdateArgs args, String eduStreamName) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
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
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                WITH updated AS (
                    UPDATE student
                    SET exported_at = now()
                    WHERE edu_stream_name = ?
                    RETURNING *
                )
                SELECT u.*,
                    pf.code as pf_code,
                    pf.display_name as pf_display_name
                FROM updated u
                LEFT JOIN practice_format pf ON u.practice_format_id = pf.id;
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
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                    UPDATE student SET
                        isu = ?,
                        st_group = ?,
                        fullname = ?,
                        status = ?,
                        application = ?,
                        notifications = ?,
                        comments = ?,
                        call_status_comments = ?,
                        practice_place = ?,
                        practice_format = ?,
                        practice_format_id = COALESCE(?, (SELECT id FROM practice_format WHERE lower(code) = lower(?) LIMIT 1)),
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
                statement.setString(5, student.getApplication() == null ? "" : student.getApplication());
                statement.setString(6, student.getNotifications() == null ? "" : student.getNotifications());
                statement.setString(7, student.getComments() == null ? "" : student.getComments());
                statement.setString(8, student.getCallStatusComments() == null ? "" : student.getCallStatusComments());
                statement.setObject(9, student.getPracticePlace(), Types.OTHER);
                statement.setObject(10, student.getPracticeFormat(), Types.OTHER);

                if (student.getPracticeFormatId() == null) {
                    statement.setNull(11, Types.BIGINT);
                } else {
                    statement.setLong(11, student.getPracticeFormatId());
                }
                statement.setString(12, student.getPracticeFormat() == null ? null : student.getPracticeFormat().name());

                Long companyINN = student.getCompanyINN();
                if (companyINN == null) {
                    statement.setNull(13, Types.INTEGER);
                } else statement.setLong(13, student.getCompanyINN());

                statement.setString(14, student.getCompanyName());
                statement.setString(15, student.getCompanyLeadFullName());
                statement.setString(16, student.getCompanyLeadPhone());
                statement.setString(17, student.getCompanyLeadEmail());
                statement.setString(18, student.getCompanyLeadJobTitle());
                statement.setString(19, student.getCellHexColor().equals("000000") ? "FFFFFF" : student.getCellHexColor());
                statement.setBoolean(20, student.isManagedManually());
                statement.setBytes(21, student.getApplicationBytes());
                statement.setString(22, student.getSignedPhotoPath());

                if (student.getTelegramUser() != null) {
                    statement.setLong(23, student.getTelegramUser().getChatId());
                    statement.setLong(24, student.getTelegramUser().getChatId());
                } else {
                    statement.setNull(23, Types.BIGINT);
                    statement.setNull(24, Types.BIGINT);
                }

                statement.setInt(25, student.getIsu());
                statement.setString(26, student.getEduStream().getName());

                updated.add(statement.executeUpdate());
            }

            return updated.stream().mapToInt(i -> i).toArray();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean updateSignedPhotoPath(long chatId, String eduStreamName, String photoPath) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(
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
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(
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

    public static boolean updateStatusByChatIdAndEduStreamName(long chatId, String eduStreamName, StudentStatus status) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(
                     "UPDATE student SET status = ?, updated_at = now() WHERE chat_id = ? AND edu_stream_name = ?;"
             )) {
            statement.setObject(1, status, Types.OTHER);
            statement.setLong(2, chatId);
            statement.setString(3, eduStreamName);
            return 1 == statement.executeUpdate();
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean updatePracticeFormatAndResetApplication(
            long chatId,
            String eduStreamName,
            PracticeFormat legacyPracticeFormat,
            Long practiceFormatId
    ) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
            var statement = connection.prepareStatement("""
                UPDATE student
                SET practice_format = ?,
                    practice_format_id = COALESCE(?, (SELECT id FROM practice_format WHERE lower(code) = lower(?) LIMIT 1)),
                    application_bytes = NULL,
                    status = CASE
                        WHEN status IN ('APPLICATION_WAITING_APPROVAL', 'APPLICATION_WAITING_SIGNING', 'APPLICATION_SIGNED')
                            THEN 'APPLICATION_WAITING_SUBMISSION'::st_status
                        WHEN status = 'APPLICATION_RETURNED'
                            THEN 'APPLICATION_RETURNED'::st_status
                        ELSE status
                    END,
                    updated_at = now()
                WHERE chat_id = ? AND edu_stream_name = ?;
                """)) {
            statement.setObject(1, legacyPracticeFormat, Types.OTHER);
            if (practiceFormatId == null) {
                statement.setNull(2, Types.BIGINT);
            } else {
                statement.setLong(2, practiceFormatId);
            }
            statement.setString(3, legacyPracticeFormat == null ? null : legacyPracticeFormat.name());
            statement.setLong(4, chatId);
            statement.setString(5, eduStreamName);
            return 1 == statement.executeUpdate();
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static List<Long> findChatIdsByPracticeFormatIdInActiveStreams(long practiceFormatId) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
            var st = connection.prepareStatement("""
                SELECT s.chat_id
                FROM student s
                JOIN edu_stream es ON es.name = s.edu_stream_name
                WHERE es.date_to > now()::date
                  AND s.chat_id IS NOT NULL
                  AND s.practice_format_id = ?;
                """)) {
            st.setLong(1, practiceFormatId);
            var rs = st.executeQuery();
            List<Long> res = new ArrayList<>();
            while (rs.next()) {
                res.add(rs.getLong("chat_id"));
            }
            return res;
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static int resetApplicationsByPracticeFormatIdInActiveStreams(long practiceFormatId, boolean clearPracticeFormat) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
            var st = connection.prepareStatement("""
                UPDATE student s
                SET application_bytes = NULL,
                    status = CASE
                        WHEN s.status IN ('APPLICATION_WAITING_APPROVAL', 'APPLICATION_WAITING_SIGNING', 'APPLICATION_SIGNED')
                            THEN 'APPLICATION_WAITING_SUBMISSION'::st_status
                        WHEN s.status = 'APPLICATION_RETURNED'
                            THEN 'APPLICATION_RETURNED'::st_status
                        ELSE s.status
                    END,
                    practice_format_id = CASE WHEN ? THEN NULL ELSE s.practice_format_id END,
                    practice_format = CASE WHEN ? THEN 'NOT_SPECIFIED'::st_practice_format ELSE s.practice_format END,
                    updated_at = now()
                FROM edu_stream es
                WHERE es.name = s.edu_stream_name
                  AND es.date_to > now()::date
                  AND s.practice_format_id = ?;
                """)) {
            st.setBoolean(1, clearPracticeFormat);
            st.setBoolean(2, clearPracticeFormat);
            st.setLong(3, practiceFormatId);
            return st.executeUpdate();
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
                    rs.getString("application"),
                    rs.getString("notifications"),
                    rs.getString("comments"),
                    rs.getString("call_status_comments"),
                    PracticePlace.valueOfIgnoreCase(rs.getString("practice_place")),
                    PracticeFormat.valueOfIgnoreCase(rs.getString("practice_format")),
                    rs.getObject("practice_format_id", Long.class),
                    null,
                    null,
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

    public static boolean updatePracticeOption(long chatId, String eduStreamName, long optionId) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                     UPDATE student
                     SET practice_option_id = ?, updated_at = now()
                     WHERE chat_id = ? AND edu_stream_name = ?;
                     """)) {
            statement.setLong(1, optionId);
            statement.setLong(2, chatId);
            statement.setString(3, eduStreamName);
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean updateSingleCompanyLeadField(long chatId, String eduStreamName, String column, String value) throws InternalException {
        var allowed = Set.of("company_lead_fullname", "company_lead_phone", "company_lead_email", "company_lead_job_title");
        if (!allowed.contains(column)) {
            throw new InternalException("Недопустимое поле: " + column);
        }
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(
                     "UPDATE student SET " + column + " = ?, updated_at = now() WHERE chat_id = ? AND edu_stream_name = ?;"
             )) {
            statement.setString(1, value);
            statement.setLong(2, chatId);
            statement.setString(3, eduStreamName);
            return 1 == statement.executeUpdate();
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static List<Student> findByGroupAndFullNamePatternAndEduStreamName(String group, String fullNamePattern, String eduStreamName) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(
                     "SELECT * FROM student WHERE st_group = ? AND LOWER(fullname) LIKE LOWER(?) AND edu_stream_name = ?;"
             )) {
            statement.setString(1, group);
            statement.setString(2, "%" + fullNamePattern + "%");
            statement.setString(3, eduStreamName);
            var rs = statement.executeQuery();
            List<Student> result = mapToStudentList(rs);
            result.sort(Comparator.comparing(Student::getFullName));
            return result;
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean existsByPracticeOptionId(long optionId) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                     SELECT 1
                     FROM student
                     WHERE practice_option_id = ?
                     LIMIT 1;
                     """)) {
            statement.setLong(1, optionId);
            var rs = statement.executeQuery();
            return rs.next();
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static void updateByIsuAndEduStream(List<Student> students) throws InternalException {
        String sql = "UPDATE student SET fullname = ?, st_group = ?, updated_at = now() WHERE isu = ? AND edu_stream_name = ?";
        try (var connection = DatabaseManager.getConnection();
             var ps = connection.prepareStatement(sql)) {
            for (Student s : students) {
                ps.setString(1, s.getFullName());
                ps.setString(2, s.getStGroup());
                ps.setInt(3, s.getIsu());
                ps.setString(4, s.getEduStream().getName());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw handleAndWrapSQLException(e);
        }
    }
}
