package ru.itmo.infra.storage;

import lombok.extern.java.Log;
import ru.itmo.domain.model.PracticeFormatEntity;
import ru.itmo.exception.InternalException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Log
public class PracticeFormatRepository {

    public static List<PracticeFormatEntity> findAllActive() throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var st = connection.prepareStatement("""
                SELECT id, code, display_name, is_active
                FROM practice_format
                WHERE is_active = true
                ORDER BY id;
                """)) {
            var rs = st.executeQuery();
            return mapList(rs);
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static Optional<PracticeFormatEntity> findById(long id) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var st = connection.prepareStatement("""
                SELECT id, code, display_name, is_active
                FROM practice_format
                WHERE id = ?;
                """)) {
            st.setLong(1, id);
            var rs = st.executeQuery();
            return mapOptional(rs);
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static Optional<PracticeFormatEntity> findByDisplayNameIgnoreCase(String displayName) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var st = connection.prepareStatement("""
                SELECT id, code, display_name, is_active
                FROM practice_format
                WHERE lower(display_name) = lower(?)
                LIMIT 1;
                """)) {
            st.setString(1, displayName);
            var rs = st.executeQuery();
            return mapOptional(rs);
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static Optional<PracticeFormatEntity> findByCodeIgnoreCase(String code) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var st = connection.prepareStatement("""
                SELECT id, code, display_name, is_active
                FROM practice_format
                WHERE lower(code) = lower(?)
                LIMIT 1;
                """)) {
            st.setString(1, code);
            var rs = st.executeQuery();
            return mapOptional(rs);
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean existsByDisplayNameIgnoreCase(String displayName) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var st = connection.prepareStatement("""
                SELECT 1
                FROM practice_format
                WHERE lower(display_name) = lower(?)
                LIMIT 1;
                """)) {
            st.setString(1, displayName);
            var rs = st.executeQuery();
            return rs.next();
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static PracticeFormatEntity create(String displayName) throws InternalException {
        String code = "CUSTOM_" + UUID.randomUUID();
        try (var connection = DatabaseManager.getConnection();
             var st = connection.prepareStatement("""
                INSERT INTO practice_format (code, display_name, is_active)
                VALUES (?, ?, true)
                RETURNING id, code, display_name, is_active;
                """)) {
            st.setString(1, code);
            st.setString(2, displayName);
            var rs = st.executeQuery();
            var created = mapOne(rs);
            if (created == null) {
                throw new InternalException("Не удалось создать формат практики");
            }
            return created;
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean renameById(long id, String newDisplayName) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var st = connection.prepareStatement("""
                UPDATE practice_format
                SET display_name = ?, updated_at = now()
                WHERE id = ?;
                """)) {
            st.setString(1, newDisplayName);
            st.setLong(2, id);
            return 1 == st.executeUpdate();
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean deleteById(long id) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var st = connection.prepareStatement("""
                DELETE FROM practice_format
                WHERE id = ?;
                """)) {
            st.setLong(1, id);
            return 1 == st.executeUpdate();
        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    private static Optional<PracticeFormatEntity> mapOptional(ResultSet rs) throws SQLException {
        var entity = mapOne(rs);
        return entity == null ? Optional.empty() : Optional.of(entity);
    }

    private static List<PracticeFormatEntity> mapList(ResultSet rs) throws SQLException {
        List<PracticeFormatEntity> res = new ArrayList<>();
        PracticeFormatEntity e = mapOne(rs);
        while (e != null) {
            res.add(e);
            e = mapOne(rs);
        }
        return res;
    }

    private static PracticeFormatEntity mapOne(ResultSet rs) throws SQLException {
        if (!rs.next()) return null;
        return PracticeFormatEntity.builder()
                .id(rs.getLong("id"))
                .code(rs.getString("code"))
                .displayName(rs.getString("display_name"))
                .active(rs.getBoolean("is_active"))
                .build();
    }

    private static InternalException handleAndWrapSQLException(SQLException ex) {
        log.severe("Ошибка во время выполнения SQL запроса: " + ex.getMessage());
        return new InternalException("Что-то пошло не так", ex.getCause());
    }
}

