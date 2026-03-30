package ru.itmo.infra.storage;

import lombok.extern.java.Log;
import ru.itmo.domain.model.EduStream;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log
public class EduStreamRepository {

    private static final Connection connection = DatabaseManager.getConnection();

    public static void save(EduStream eduStream) throws InternalException {
        try (var statement = connection.prepareStatement(
                "INSERT INTO edu_stream (name, year, date_from, date_to) VALUES (?, ?, ?, ?);"
        )) {
            statement.setString(1, eduStream.getName());
            statement.setInt(2, eduStream.getYear());
            statement.setDate(3, Date.valueOf(eduStream.getDateFrom()));
            statement.setDate(4, Date.valueOf(eduStream.getDateTo()));
            statement.executeUpdate();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static List<EduStream> findAll() throws InternalException {
        try (var statement = connection.prepareStatement(
                "SELECT * FROM edu_stream;"
        )) {
            var rs = statement.executeQuery();
            List<EduStream> result = new ArrayList<>();

            EduStream eduStream = mapToEduStream(rs);
            while (eduStream != null) {
                result.add(eduStream);
                eduStream = mapToEduStream(rs);
            }

            return result;

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static List<String> findAllNames() throws InternalException {
        try (var statement = connection.prepareStatement(
                "SELECT name FROM edu_stream ORDER BY date_from DESC;"
        )) {
            var rs = statement.executeQuery();
            List<String> result = new ArrayList<>();

            while (rs.next()) {
                result.add(rs.getString("name"));
            }

            return result;

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static List<String> findAllGroupsByStreamName(EduStream eduStream) throws InternalException {
        try (var statement = connection.prepareStatement("""
                    SELECT student.st_group
                    FROM edu_stream LEFT JOIN student ON edu_stream.name = student.edu_stream_name
                    WHERE edu_stream.name = ?
                    GROUP BY student.st_group;
                """
        )) {
            statement.setString(1, eduStream.getName());
            var rs = statement.executeQuery();
            List<String> result = new ArrayList<>();

            while (rs.next())
                result.add(rs.getString("st_group"));

            return result.stream().sorted().toList();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean existsByName(EduStream eduStream) throws InternalException {
        try (var statement = connection.prepareStatement(
                "SELECT * FROM edu_stream WHERE name = ?;"
        )) {
            statement.setString(1, eduStream.getName());
            var rs = statement.executeQuery();
            return rs.next();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static Optional<EduStream> findByName(EduStream eduStream) throws InternalException {
        try (var statement = connection.prepareStatement(
                "SELECT * FROM edu_stream WHERE name = ?;"
        )) {
            statement.setString(1, eduStream.getName());
            var rs = statement.executeQuery();
            return mapToEduStreamOptional(rs);

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean deleteByName(EduStream eduStream) throws InternalException {
        try (var statement = connection.prepareStatement(
                "DELETE FROM edu_stream WHERE name = ?;"
        )) {
            statement.setString(1, eduStream.getName());
            return 1 == statement.executeUpdate();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean updateByName(EduStream oldEduStream, EduStream newEduStream) throws InternalException {
        try (var statement = connection.prepareStatement(
                "UPDATE edu_stream SET name = ?, year = ?, date_from = ?, date_to = ? WHERE name = ?;"
        )) {
            statement.setString(1, newEduStream.getName());
            statement.setInt(2, newEduStream.getYear());
            statement.setDate(3, Date.valueOf(newEduStream.getDateFrom()));
            statement.setDate(4, Date.valueOf(newEduStream.getDateTo()));
            statement.setString(5, oldEduStream.getName());
            return 1 == statement.executeUpdate();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    private static Optional<EduStream> mapToEduStreamOptional(ResultSet rs) throws SQLException, InternalException {
        EduStream eduStream = mapToEduStream(rs);
        if (eduStream == null) return Optional.empty();
        return Optional.of(eduStream);
    }

    private static EduStream mapToEduStream(ResultSet rs) throws SQLException, InternalException {
        if (rs.next()) {
            try {
                return new EduStream(
                        rs.getString("name"),
                        rs.getInt("year"),
                        rs.getDate("date_from").toLocalDate(),
                        rs.getDate("date_to").toLocalDate()
                );
            } catch (BadRequestException ex) {
                log.severe("Нарушена консистентность данных. Попытка смаппить EduStream.\nОшибка: " + ex.getMessage());
                throw new InternalException("Ошибка чтения данных из базы: нарушена консистентность данных учебных потоков");
            }
        }
        return null;
    }

    private static InternalException handleAndWrapSQLException(SQLException ex) {
        log.severe("Ошибка во время выполнения SQL запроса: " + ex.getMessage());
        return new InternalException("Что-то пошло не так", ex.getCause());
    }
}
