package ru.itmo.infra.storage;

import lombok.extern.java.Log;
import ru.itmo.domain.model.TelegramUser;
import ru.itmo.exception.InternalException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log
public class TelegramUserRepository {

    private static final Connection connection = DatabaseManager.getConnection();

    public static void save(TelegramUser telegramUser) throws InternalException {
        try (var statement = connection.prepareStatement(
                "INSERT INTO tg_user (chat_id, is_admin, is_banned, username) VALUES (?, ?, ?, ?)"
        )) {
            statement.setLong(1, telegramUser.getChatId());
            statement.setBoolean(2, telegramUser.isAdmin());
            statement.setBoolean(3, telegramUser.isBanned());
            statement.setString(4, telegramUser.getUsername());
            statement.executeUpdate();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static void saveTransactional(TelegramUser telegramUser, Connection transactionConnection) throws SQLException {
        try (var statement = transactionConnection.prepareStatement(
                "INSERT INTO tg_user (chat_id, is_admin, is_banned, username) VALUES (?, ?, ?, ?)"
        )) {
            statement.setLong(1, telegramUser.getChatId());
            statement.setBoolean(2, telegramUser.isAdmin());
            statement.setBoolean(3, telegramUser.isBanned());
            statement.setString(4, telegramUser.getUsername());
            statement.executeUpdate();
        }
    }

    public static List<TelegramUser> findAll() throws InternalException {
        try (var statement = connection.prepareStatement(
                "SELECT * FROM tg_user;"
        )) {
            var rs = statement.executeQuery();
            return mapToTelegramUserList(rs);

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static List<TelegramUser> findAllNotBannedAdmins() throws InternalException {
        try (var statement = connection.prepareStatement(
                "SELECT * FROM tg_user WHERE is_admin = true AND is_banned = false;"
        )) {
            var rs = statement.executeQuery();
            return mapToTelegramUserList(rs);

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static List<TelegramUser> findAllAdmins() throws InternalException {
        try (var statement = connection.prepareStatement(
                "SELECT * FROM tg_user WHERE is_admin = true;"
        )) {
            var rs = statement.executeQuery();
            return mapToTelegramUserList(rs);

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean existsByChatId(long chatId) throws InternalException {
        try (var statement = connection.prepareStatement(
                "SELECT * FROM tg_user WHERE chat_id = ?;"
        )) {
            statement.setLong(1, chatId);
            var rs = statement.executeQuery();
            return rs.next();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static Optional<TelegramUser> findByChatId(long chatId) throws InternalException {
        try (var statement = connection.prepareStatement(
                "SELECT * FROM tg_user WHERE chat_id = ?;"
        )) {
            statement.setLong(1, chatId);
            var rs = statement.executeQuery();
            return mapToTelegramUserOptional(rs);

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean deleteByChatId(long chatId) throws InternalException {
        try (var statement = connection.prepareStatement(
                "DELETE FROM tg_user WHERE chat_id = ?;"
        )) {
            statement.setLong(1, chatId);
            return 1 == statement.executeUpdate();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean updateByChatId(TelegramUser telegramUser) throws InternalException {
        try (var statement = connection.prepareStatement(
                "UPDATE tg_user SET is_admin = ?, is_banned = ?, username = ? WHERE chat_id = ?;"
        )) {
            statement.setBoolean(1, telegramUser.isAdmin());
            statement.setBoolean(2, telegramUser.isBanned());
            statement.setString(3, telegramUser.getUsername());
            statement.setLong(4, telegramUser.getChatId());
            return 1 == statement.executeUpdate();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static Long getAdminsCount() throws InternalException {
        try (var statement = connection.prepareStatement(
                "SELECT COUNT(*) as total FROM tg_user WHERE is_admin = true;"
        )) {
            var rs = statement.executeQuery();
            rs.next();
            return rs.getLong("total");

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static List<TelegramUser> findAllBanned() throws InternalException {
        try (var statement = connection.prepareStatement(
                "SELECT * FROM tg_user WHERE is_banned = true;"
        )) {
            var rs = statement.executeQuery();
            return mapToTelegramUserList(rs);

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    private static Optional<TelegramUser> mapToTelegramUserOptional(ResultSet rs) throws SQLException {
        TelegramUser telegramUser = mapToTelegramUser(rs);
        if (telegramUser == null) return Optional.empty();
        return Optional.of(telegramUser);
    }

    private static TelegramUser mapToTelegramUser(ResultSet rs) throws SQLException {
        if (rs.next()) {
            return new TelegramUser(
                    rs.getLong("chat_id"),
                    rs.getBoolean("is_admin"),
                    rs.getBoolean("is_banned"),
                    rs.getString("username")
            );
        }
        return null;
    }

    private static List<TelegramUser> mapToTelegramUserList(ResultSet rs) throws SQLException {
        List<TelegramUser> result = new ArrayList<>();

        TelegramUser telegramUser = mapToTelegramUser(rs);
        while (telegramUser != null) {
            result.add(telegramUser);
            telegramUser = mapToTelegramUser(rs);
        }

        return result;
    }

    private static InternalException handleAndWrapSQLException(SQLException ex) {
        log.severe("Ошибка во время выполнения SQL запроса: " + ex.getMessage());
        return new InternalException("Что-то пошло не так", ex.getCause());
    }
}
