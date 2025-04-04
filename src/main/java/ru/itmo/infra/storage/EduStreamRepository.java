package ru.itmo.infra.storage;

import lombok.extern.java.Log;
import ru.itmo.domain.model.EduStream;
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

    public static long save(EduStream eduStream) throws InternalException {
        try (var statement = connection.prepareStatement(
                "INSERT INTO edu_stream (name, year, date_from, date_to) VALUES (?, ?, ?, ?) RETURNING id;"
        )) {
            statement.setString(1, eduStream.getName());
            statement.setInt(2, eduStream.getYear());
            statement.setDate(3, Date.valueOf(eduStream.getDateFrom()));
            statement.setDate(4, Date.valueOf(eduStream.getDateTo()));
            ResultSet rs = statement.executeQuery();
            rs.next();
            return rs.getLong("id");

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

    public static boolean existsById(long id) throws InternalException {
        try (var statement = connection.prepareStatement(
                "SELECT * FROM edu_stream WHERE id = ?;"
        )) {
            statement.setLong(1, id);
            var rs = statement.executeQuery();
            return rs.next();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean existsByName(String name) throws InternalException {
        try (var statement = connection.prepareStatement(
                "SELECT * FROM edu_stream WHERE name = ?;"
        )) {
            statement.setString(1, name);
            var rs = statement.executeQuery();
            return rs.next();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static Optional<EduStream> findById(long id) throws InternalException {
        try (var statement = connection.prepareStatement(
                "SELECT * FROM edu_stream WHERE id = ?;"
        )) {
            statement.setLong(1, id);
            var rs = statement.executeQuery();
            return mapToEduStreamOptional(rs);

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static Optional<EduStream> findByName(String name) throws InternalException {
        try (var statement = connection.prepareStatement(
                "SELECT * FROM edu_stream WHERE name = ?;"
        )) {
            statement.setString(1, name);
            var rs = statement.executeQuery();
            return mapToEduStreamOptional(rs);

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean deleteById(long id) throws InternalException {
        try (var statement = connection.prepareStatement(
                "DELETE FROM edu_stream WHERE id = ?;"
        )) {
            statement.setLong(1, id);
            return 1 == statement.executeUpdate();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean deleteByName(String name) throws InternalException {
        try (var statement = connection.prepareStatement(
                "DELETE FROM edu_stream WHERE name = ?;"
        )) {
            statement.setString(1, name);
            return 1 == statement.executeUpdate();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean updateById(EduStream eduStream) throws InternalException {
        try (var statement = connection.prepareStatement(
                "UPDATE edu_stream SET name = ?, year = ?, date_from = ?, date_to = ? WHERE id = ?;"
        )) {
            statement.setString(1, eduStream.getName());
            statement.setInt(2, eduStream.getYear());
            statement.setDate(3, Date.valueOf(eduStream.getDateFrom()));
            statement.setDate(4, Date.valueOf(eduStream.getDateTo()));
            statement.setLong(5, eduStream.getId());
            return 1 == statement.executeUpdate();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean updateByName(EduStream eduStream) throws InternalException {
        try (var statement = connection.prepareStatement(
                "UPDATE edu_stream SET year = ?, date_from = ?, date_to = ? WHERE name = ?;"
        )) {
            statement.setInt(1, eduStream.getYear());
            statement.setDate(2, Date.valueOf(eduStream.getDateFrom()));
            statement.setDate(3, Date.valueOf(eduStream.getDateTo()));
            statement.setString(4, eduStream.getName());
            return 1 == statement.executeUpdate();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    private static Optional<EduStream> mapToEduStreamOptional(ResultSet rs) throws SQLException {
        EduStream eduStream = mapToEduStream(rs);
        if (eduStream == null) return Optional.empty();
        return Optional.of(eduStream);
    }

    private static EduStream mapToEduStream(ResultSet rs) throws SQLException {
        if (rs.next()) {
            return new EduStream(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getInt("year"),
                    rs.getDate("date_from").toLocalDate(),
                    rs.getDate("date_to").toLocalDate()
            );
        }
        return null;
    }

    private static InternalException handleAndWrapSQLException(SQLException ex) {
        log.severe("Ошибка во время выполнения SQL запроса: " + ex.getMessage());
        return new InternalException("Что-то пошло не так", ex.getCause());
    }
}
