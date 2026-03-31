package ru.itmo.infra.storage;

import lombok.extern.java.Log;
import ru.itmo.domain.model.AdminToken;
import ru.itmo.exception.InternalException;

import java.sql.Connection;
import java.sql.SQLException;

@Log
public class AdminTokenRepository {

    public static void save(AdminToken adminToken) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(
                "INSERT INTO admin_token (token) VALUES (?);"
        )) {
            statement.setObject(1, adminToken.getToken());
            statement.executeUpdate();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean delete(AdminToken adminToken) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(
                "DELETE FROM admin_token WHERE token = ?;"
        )) {
            statement.setObject(1, adminToken.getToken());
            return 1 == statement.executeUpdate();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean deleteTransactional(AdminToken adminToken, Connection transactionConnection)
            throws SQLException {
        try (var statement = transactionConnection.prepareStatement(
                "DELETE FROM admin_token WHERE token = ?;"
        )) {
            statement.setObject(1, adminToken.getToken());
            return 1 == statement.executeUpdate();
        }
    }

    public static boolean deleteAll() throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(
                "DELETE FROM admin_token;"
        )) {
            return statement.executeUpdate() > 0;

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static boolean exists(AdminToken adminToken) throws InternalException {
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(
                "SELECT 1 FROM admin_token WHERE token = ? LIMIT 1;"
        )) {
            statement.setObject(1, adminToken.getToken());
            return statement.executeQuery().next();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    private static InternalException handleAndWrapSQLException(SQLException ex) {
        log.severe("Ошибка во время выполнения SQL запроса: " + ex.getMessage());
        return new InternalException("Что-то пошло не так", ex.getCause());
    }

}
