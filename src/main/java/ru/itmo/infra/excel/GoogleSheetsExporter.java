package ru.itmo.infra.excel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GoogleSheetsExporter {
    private static final String csvUrl = "https://docs.google.com/spreadsheets/d/1sYSJ2SKp56c2iqcNlXkcoaP3JwhcLsH26lqK7HZ43DQ/export?format=csv&gid=0";

    private static InputStream downloadFile() throws IOException {
        URL url = new URL(csvUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        return connection.getInputStream();
    }

    public static boolean checkInnInCsv(long inn) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(downloadFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");

                if (columns.length > 7) {
                    String cellValue = columns[7];
                    if (cellValue.contains(String.valueOf(inn))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
