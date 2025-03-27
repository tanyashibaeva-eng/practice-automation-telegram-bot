package ru.itmo.util;

import lombok.extern.java.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

@Log
public class PropertiesProvider {
    private static final String RESOURCES_PATH = requireNonNull(PropertiesProvider.class.getClassLoader().getResource("")).getPath();
    private static final String PROPERTIES_FILENAME = "application.properties";
    private static final Properties properties;

    static {
        String appPropertiesFilepath = RESOURCES_PATH + PROPERTIES_FILENAME;
        properties = loadProperties(appPropertiesFilepath);
    }

    private static Properties loadProperties(String path) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(path));
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
