package ru.itmo.util;

import lombok.Getter;
import lombok.extern.java.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Log
public class PropertiesProvider {

    private static final String PROPERTIES_FILE_PATH = "src/main/resources/application.properties";

    @Getter
    private static final String token;
    @Getter
    private static final String dsn;
    private static final Properties properties;

    static {
        properties = loadProperties();
        token = loadToken();
        dsn = loadDsn();
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(PROPERTIES_FILE_PATH)) {
            properties.load(fis);
        } catch (IOException ex) {
            log.severe("Could not load properties file: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
        return properties;
    }

    private static String loadToken() {
        String token = System.getenv("BOT_TOKEN");
        if (token == null)
            throw new RuntimeException("BOT_TOKEN variable in .env file is not set");
        return token;
    }

    private static String loadDsn() {
        String dsn = System.getenv("PG_DSN");
        if (dsn == null)
            throw new RuntimeException("PG_DSN variable in .env file is not set");
        return dsn;
    }

    public static String getProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }

}
