package ru.itmo.util;

import lombok.extern.java.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

@Log
public class PropertiesProvider {
    private static final Properties properties;

    static {
        properties = loadProperties();
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/resources/application.properties")) {
            properties.load(fis);
        } catch (IOException ex) {
            log.severe(ex.getMessage());
            throw new RuntimeException(ex);
        }
        return properties;
    }



    public static String getToken() {
        return properties.getProperty("token");
    }
}
