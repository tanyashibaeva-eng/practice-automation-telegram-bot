package ru.itmo.infra.storage;

import lombok.extern.java.Log;
import ru.itmo.domain.model.Student;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.domain.type.PracticePlace;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.InternalException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

@Log
public class StudentRepository {

    private static final Connection connection = DatabaseManager.getConnection();

    public static void saveBatch(List<Student> students) throws InternalException {
        try (var statement = connection.prepareStatement("""
                    INSERT INTO student (
                        chat_id,
                        edu_stream_id,
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
            for (var student : students) {
                statement.setLong(1, student.getTelegramUser().getChatId());
                statement.setLong(2, student.getEduStream().getId());
                statement.setInt(3, student.getIsu());
                statement.setString(4, student.getStGroup());
                statement.setString(5, student.getFullName());
                statement.setObject(6, student.getStatus(), Types.OTHER);
                statement.setString(7, student.getComments());
                statement.setString(8, student.getCallStatusComments());
                statement.setObject(9, student.getPracticePlace(), Types.OTHER);
                statement.setObject(10, student.getPracticeFormat(), Types.OTHER);
                statement.setInt(11, student.getCompanyINN());
                statement.setString(12, student.getCompanyName());
                statement.setString(13, student.getCompanyLeadFullName());
                statement.setString(14, student.getCompanyLeadPhone());
                statement.setString(15, student.getCompanyLeadEmail());
                statement.setString(16, student.getCompanyLeadJobTitle());
                statement.setString(17, student.getCellHexColor());
                statement.setBoolean(18, student.isManagedManually());
                statement.addBatch();
            }
            statement.executeBatch();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static List<Student> findAll() throws InternalException {
        try (var statement = connection.prepareStatement(
                "SELECT * FROM student;"
        )) {
            var rs = statement.executeQuery();
            List<Student> result = new ArrayList<>();

            Student student = mapToStudent(rs);
            while (student != null) {
                result.add(student);
                student = mapToStudent(rs);
            }

            return result;

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
            List<Student> result = new ArrayList<>();

            Student student = mapToStudent(rs);
            while (student != null) {
                result.add(student);
                student = mapToStudent(rs);
            }

            return result;

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    private static String buildFilteringQuery(Filter filter) {
        String query = "SELECT * FROM student WHERE ";
        StringJoiner stringJoiner = new StringJoiner(" AND ");

        Long eduStreamId = filter.getEduStreamId();
        if (eduStreamId != null)
            stringJoiner.add("edu_stream_id = %d".formatted(eduStreamId));

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

    public static boolean existsByChatIdAndEduStreamId(long chatId, long eduStreamId) throws InternalException {
        try (var statement = connection.prepareStatement(
                "SELECT * FROM student WHERE chat_id = ? AND edu_stream_id = ?;"
        )) {
            statement.setLong(1, chatId);
            statement.setLong(1, eduStreamId);
            var rs = statement.executeQuery();
            return rs.next();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static Optional<Student> findByChatIdAndEduStreamId(long chatId, long eduStreamId) throws InternalException {
        try (var statement = connection.prepareStatement(
                "SELECT * FROM student WHERE chat_id = ? AND edu_stream_id = ?;"
        )) {
            statement.setLong(1, chatId);
            statement.setLong(2, eduStreamId);
            var rs = statement.executeQuery();
            return mapToStudentOptional(rs);

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean deleteByChatIdAndEduStreamId(long chatId, long eduStreamId) throws InternalException {
        try (var statement = connection.prepareStatement(
                "DELETE FROM student WHERE chat_id = ? AND edu_stream_id = ?;"
        )) {
            statement.setLong(1, chatId);
            statement.setLong(2, eduStreamId);
            return 1 == statement.executeUpdate();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static int[] updateBatchByChatIdAndEduStreamId(List<Student> students) throws InternalException {
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
                        managed_manually = ?
                    WHERE chat_id = ? AND edu_stream_id = ?;
                """
        )) {
            for (var student : students) {
                statement.setInt(1, student.getIsu());
                statement.setString(2, student.getStGroup());
                statement.setString(3, student.getFullName());
                statement.setObject(4, student.getStatus(), Types.OTHER);
                statement.setString(5, student.getComments());
                statement.setString(6, student.getCallStatusComments());
                statement.setObject(7, student.getPracticePlace(), Types.OTHER);
                statement.setObject(8, student.getPracticeFormat(), Types.OTHER);
                statement.setInt(9, student.getCompanyINN());
                statement.setString(10, student.getCompanyName());
                statement.setString(11, student.getCompanyLeadFullName());
                statement.setString(12, student.getCompanyLeadPhone());
                statement.setString(13, student.getCompanyLeadEmail());
                statement.setString(14, student.getCompanyLeadJobTitle());
                statement.setString(15, student.getCellHexColor());
                statement.setBoolean(16, student.isManagedManually());
                statement.setLong(17, student.getTelegramUser().getChatId());
                statement.setLong(18, student.getEduStream().getId());
                statement.addBatch();
            }

            return statement.executeBatch();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    private static Optional<Student> mapToStudentOptional(ResultSet rs) throws SQLException, InternalException {
        Student student = mapToStudent(rs);
        if (student == null) return Optional.empty();
        return Optional.of(student);
    }

    private static Student mapToStudent(ResultSet rs) throws SQLException, InternalException {
        if (rs.next()) {
            return new Student(
                    TelegramUserRepository.findByChatId(rs.getLong("chat_id"))
                            .orElseThrow(() -> new InternalException("Студент с таким chatId не зарегистрирован")),
                    EduStreamRepository.findById(rs.getLong("edu_stream_id"))
                            .orElseThrow(() -> new InternalException("Поток с таким id не найден")),
                    rs.getInt("isu"),
                    rs.getString("st_group"),
                    rs.getString("fullname"),
                    StudentStatus.valueOfIgnoreCase(rs.getString("status")),
                    rs.getString("comments"),
                    rs.getString("call_status_comments"),
                    PracticePlace.valueOfIgnoreCase(rs.getString("practice_place")),
                    PracticeFormat.valueOfIgnoreCase(rs.getString("practice_format")),
                    rs.getInt("company_inn"),
                    rs.getString("company_name"),
                    rs.getString("company_lead_fullname"),
                    rs.getString("company_lead_phone"),
                    rs.getString("company_lead_email"),
                    rs.getString("company_lead_job_title"),
                    rs.getString("cell_hex_color"),
                    rs.getBoolean("managed_manually")
            );
        }
        return null;
    }

    private static InternalException handleAndWrapSQLException(SQLException ex) {
        log.severe("Ошибка во время выполнения SQL запроса: " + ex.getMessage());
        return new InternalException("Что-то пошло не так", ex.getCause());
    }
}
