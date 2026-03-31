package ru.itmo.infra.storage;

import lombok.extern.java.Log;
import ru.itmo.domain.model.PracticeOption;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log
public class PracticeOptionRepository {
    public static List<PracticeOption> findAll() throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                     SELECT id, title, enabled, requires_itmo_info, requires_company_info
                     FROM practice_option
                     ORDER BY id;
                     """)) {
            var rs = statement.executeQuery();
            var result = new ArrayList<PracticeOption>();
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static List<PracticeOption> findAllEnabled() throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                     SELECT id, title, enabled, requires_itmo_info, requires_company_info
                     FROM practice_option
                     WHERE enabled = TRUE
                     ORDER BY id;
                     """)) {
            var rs = statement.executeQuery();
            var result = new ArrayList<PracticeOption>();
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static Optional<PracticeOption> findById(long id) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                     SELECT id, title, enabled, requires_itmo_info, requires_company_info
                     FROM practice_option
                     WHERE id = ?;
                     """)) {
            statement.setLong(1, id);
            var rs = statement.executeQuery();
            if (!rs.next()) {
                return Optional.empty();
            }
            return Optional.of(map(rs));
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static Optional<PracticeOption> findByTitle(String title) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                     SELECT id, title, enabled, requires_itmo_info, requires_company_info
                     FROM practice_option
                     WHERE title = ?;
                     """)) {
            statement.setString(1, title);
            var rs = statement.executeQuery();
            if (!rs.next()) {
                return Optional.empty();
            }
            return Optional.of(map(rs));
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static PracticeOption create(String title, boolean requiresItmoInfo, boolean requiresCompanyInfo) throws InternalException, BadRequestException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                     INSERT INTO practice_option (title, enabled, requires_itmo_info, requires_company_info)
                     VALUES (?, TRUE, ?, ?)
                     RETURNING id, title, enabled, requires_itmo_info, requires_company_info;
                     """)) {
            statement.setString(1, title);
            statement.setBoolean(2, requiresItmoInfo);
            statement.setBoolean(3, requiresCompanyInfo);
            var rs = statement.executeQuery();
            if (rs.next()) {
                return map(rs);
            }
            throw new InternalException("Не удалось создать вариант места практики");
        } catch (SQLException ex) {
            if ("23505".equals(ex.getSQLState())) {
                throw new BadRequestException("Вариант с таким названием уже существует");
            }
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean updateEnabled(long id, boolean enabled) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                     UPDATE practice_option
                     SET enabled = ?
                     WHERE id = ?;
                     """)) {
            statement.setBoolean(1, enabled);
            statement.setLong(2, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean updateTitle(long id, String newTitle) throws InternalException, BadRequestException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                     UPDATE practice_option
                     SET title = ?
                     WHERE id = ?;
                     """)) {
            statement.setString(1, newTitle);
            statement.setLong(2, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            if ("23505".equals(ex.getSQLState())) {
                throw new BadRequestException("Вариант с таким названием уже существует");
            }
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean updateFlags(long id, boolean requiresItmoInfo, boolean requiresCompanyInfo) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                     UPDATE practice_option
                     SET requires_itmo_info = ?, requires_company_info = ?
                     WHERE id = ?;
                     """)) {
            statement.setBoolean(1, requiresItmoInfo);
            statement.setBoolean(2, requiresCompanyInfo);
            statement.setLong(3, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean deleteById(long id) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement("""
                     DELETE FROM practice_option
                     WHERE id = ?;
                     """)) {
            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    private static PracticeOption map(java.sql.ResultSet rs) throws SQLException {
        return PracticeOption.builder()
                .id(rs.getLong("id"))
                .title(rs.getString("title"))
                .enabled(rs.getBoolean("enabled"))
                .requiresItmoInfo(rs.getBoolean("requires_itmo_info"))
                .requiresCompanyInfo(rs.getBoolean("requires_company_info"))
                .build();
    }

    private static InternalException handleAndWrapSQLException(SQLException ex) {
        log.severe("Ошибка во время выполнения SQL запроса: " + ex.getMessage());
        return new InternalException("Что-то пошло не так", ex.getCause());
    }
}
