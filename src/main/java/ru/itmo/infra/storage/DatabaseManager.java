package ru.itmo.infra.storage;

import lombok.extern.java.Log;
import ru.itmo.util.PropertiesProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Log
public class DatabaseManager {

    private static final String INIT_SCRIPT_PATH = "src/main/resources/migrations/init.sql";

    static {
        try (Connection initConnection = createConnection()) {
            String sql = loadScript();
            try (var statement = initConnection.createStatement()) {
                statement.executeUpdate(sql);
            }
        } catch (SQLException ex) {
            log.severe(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public static Connection getConnection() {
        return createConnection();
    }

    public static Connection initializeConnection() {
        return createConnection();
    }

    private static Connection createConnection() {
        try {
            return DriverManager.getConnection(PropertiesProvider.getDsn());
        } catch (SQLException ex) {
            log.severe(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private static String loadScript() {
        try {
            return Files.readString(Path.of(INIT_SCRIPT_PATH));
        } catch (IOException ex) {
            log.severe("Could not read sql initialization file: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
