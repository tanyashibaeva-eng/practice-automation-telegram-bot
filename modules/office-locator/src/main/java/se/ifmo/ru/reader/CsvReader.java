package se.ifmo.ru.reader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CsvReader {
    public static Optional<RawCsvData> readRawCsvData(String csvFile) {
        List<RawCsvRecord> data = new ArrayList<>();

        try (BufferedReader br = openReader(csvFile)) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                return Optional.empty();
            }
            List<String> headers = Arrays.asList(headerLine.split(";", -1));

            String line;
            long lineNumber = 0;
            while ((line = br.readLine()) != null) {
                List<String> values = Arrays.asList(line.split(";", -1));
                RawCsvRecord rawCsvRecord = new RawCsvRecord(lineNumber, values);
                data.add(rawCsvRecord);
                lineNumber++;
            }
            return Optional.of(new RawCsvData(headers, data));

        } catch (IOException e) {
            System.err.println("Error reading the CSV file: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private static BufferedReader openReader(String csvFile) throws IOException {
        Path path = Path.of(csvFile);
        if (Files.exists(path)) {
            return Files.newBufferedReader(path, StandardCharsets.UTF_8);
        }
        InputStream is = CsvReader.class.getClassLoader().getResourceAsStream(csvFile);
        if (is == null) {
            throw new FileNotFoundException("CSV file not found: " + csvFile);
        }
        return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    }

    public static boolean writeRawCsvData(String csvFile, List<String> headers, List<RawCsvRecord> data) {
        Path path = Path.of(csvFile);

        try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            bw.write(String.join(";", headers));
            bw.newLine();

            for (RawCsvRecord record : data) {
                bw.write(String.join(";", record.getRawColumns()));
                bw.newLine();
            }

            return true;

        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean appendRawCsvRow(String csvFile, List<String> columns) {
        Path path = Path.of(csvFile);
        try (BufferedWriter bw = Files.newBufferedWriter(
                path,
                StandardCharsets.UTF_8,
                StandardOpenOption.APPEND,
                StandardOpenOption.CREATE)) {
            if (Files.exists(path) && Files.size(path) > 0) {
                long size = Files.size(path);
                try (var channel = Files.newByteChannel(path, StandardOpenOption.READ)) {
                    channel.position(size - 1);
                    byte[] lastByte = new byte[1];
                    channel.read(java.nio.ByteBuffer.wrap(lastByte));
                    if (lastByte[0] != '\n') {
                        bw.newLine();
                    }
                }
            }
            bw.write(String.join(";", columns));
            bw.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("Error appending CSV file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
