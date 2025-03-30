package ru.itmo.persistence;

import lombok.Getter;
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

    @Getter
    private static final Connection connection;

    static {
        try {
            connection = DriverManager.getConnection(PropertiesProvider.getDsn());
        } catch (SQLException ex) {
            log.severe(ex.getMessage());
            throw new RuntimeException(ex);
        }

        String sql = loadScript();
        try (var statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException ex) {
            log.severe(ex.getMessage());
            throw new RuntimeException();
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
