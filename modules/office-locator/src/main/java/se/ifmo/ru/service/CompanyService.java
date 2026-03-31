package se.ifmo.ru.service;

import lombok.extern.java.Log;
import se.ifmo.ru.exception.CompanyAlreadyExistsException;
import se.ifmo.ru.exception.CompanyNotFoundException;
import se.ifmo.ru.exception.CsvReadException;
import se.ifmo.ru.exception.CsvWriteException;
import se.ifmo.ru.exception.InvalidCompanyDataException;
import se.ifmo.ru.exception.InvalidInnException;
import se.ifmo.ru.parser.CompanyCsvParser;
import se.ifmo.ru.parser.CompanyRecord;
import se.ifmo.ru.reader.CsvReader;
import se.ifmo.ru.reader.RawCsvData;
import se.ifmo.ru.reader.RawCsvRecord;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log
public class CompanyService {
    private static final Pattern INN_PATTERN = Pattern.compile("^\\d{10}$|^\\d{12}$");

    private final HashMap<String, CompanyRecord> loadedCompanyRecords = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private String sourceCsvPath;
    private int columnCount;
    private int nextRecordNumber = 1;
    private List<String> headers = new ArrayList<>();

    private HashMap<String, CompanyRecord> convertRecordsToMapByINN(List<CompanyRecord> companyRecordList) {
        HashMap<String, CompanyRecord> companyRecordHashMap = new HashMap<>();
        int duplicateCount = 0;
        for (CompanyRecord companyRecord : companyRecordList) {
            if (companyRecordHashMap.containsKey(companyRecord.getINN())) {
                duplicateCount++;
                log.warning("Skipping duplicate INN in source data: " + companyRecord.getINN());
                continue;
            }
            companyRecordHashMap.put(companyRecord.getINN(), companyRecord);
        }
        if (duplicateCount > 0) {
            log.info("Skipped " + duplicateCount + " duplicate INN records while building registry");
        }
        return companyRecordHashMap;
    }

    private void loadCompanyRecords(String path) throws CsvReadException {
        Optional<RawCsvData> rawCsvDataOptional = CsvReader.readRawCsvData(path);
        if (rawCsvDataOptional.isEmpty()) {
            throw new CsvReadException("Empty csv file");
        }
        RawCsvData rawCsvData = rawCsvDataOptional.get();
        List<RawCsvRecord> rawCsvRecords = rawCsvData.getRecords();

        List<CompanyRecord> companyRecordList = new ArrayList<>();
        int skippedRecords = 0;
        for (RawCsvRecord rawCsvRecord : rawCsvRecords) {
            try {
                companyRecordList.add(CompanyCsvParser.parseFromRawRecord(rawCsvRecord));
            } catch (InvalidCompanyDataException e) {
                skippedRecords++;
                log.warning("Skipping invalid company record at line " + rawCsvRecord.getLineNumber() + ": " + e.getMessage());
            }
        }
        if (companyRecordList.isEmpty()) {
            throw new CsvReadException("CSV file does not contain any valid company records");
        }
        if (skippedRecords > 0) {
            log.info("Skipped " + skippedRecords + " invalid company records while loading " + path);
        }

        HashMap<String, CompanyRecord> recordsByInn = this.convertRecordsToMapByINN(companyRecordList);

        lock.writeLock().lock();
        try {
            this.loadedCompanyRecords.clear();
            this.loadedCompanyRecords.putAll(recordsByInn);
            this.sourceCsvPath = path;
            this.headers = new ArrayList<>(rawCsvData.getHeaders());
            this.columnCount = Math.max(this.headers.size(), 0);
            int maxNumber = 0;
            for (RawCsvRecord raw : rawCsvRecords) {
                List<String> columns = raw.getRawColumns();
                if (columns != null && columns.size() > this.columnCount) {
                    this.columnCount = columns.size();
                }
                if (columns != null && !columns.isEmpty()) {
                    String rawNumber = columns.get(0).trim();
                    String normalizedNumber = rawNumber.replaceAll("\\s", "");
                    if (normalizedNumber.isEmpty()) {
                        continue;
                    }
                    try {
                        int number = Integer.parseInt(normalizedNumber);
                        if (number > maxNumber) {
                            maxNumber = number;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            this.nextRecordNumber = maxNumber + 1;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public CompanyService(String path) throws CsvReadException {
        this.loadCompanyRecords(path);
    }

    public CompanyService() {
    }

    private void validateINN(String INN) throws InvalidInnException {
        if (INN == null) {
            throw new InvalidInnException("INN is null");
        }
        Matcher matcher = INN_PATTERN.matcher(INN);
        if (!matcher.matches()) {
            throw new InvalidInnException("INN must fit to regexp ^\\\\d{10}$|^\\\\d{12}$");
        }
    }

    public CompanyRecord findCompanyRecordByINN(String INN) throws InvalidInnException, CompanyNotFoundException {
        validateINN(INN);
        lock.readLock().lock();
        try {
            if (loadedCompanyRecords.containsKey(INN)) {
                return loadedCompanyRecords.get(INN);
            }
            throw new CompanyNotFoundException("Company with a such INN wasn't found, please enter the company address");
        } finally {
            lock.readLock().unlock();
        }
    }

    public void addCompanyRecord(String name, String INN, String address)
            throws InvalidInnException, InvalidCompanyDataException, CompanyAlreadyExistsException, CsvWriteException {
        if (name == null || name.isBlank()) {
            throw new InvalidCompanyDataException("Name must be provided");
        }
        if (address == null || address.isBlank()) {
            throw new InvalidCompanyDataException("Address must be provided");
        }
        validateINN(INN);
        lock.writeLock().lock();
        try {
            if (loadedCompanyRecords.containsKey(INN)) {
                throw new CompanyAlreadyExistsException("Company with such INN already exists");
            }
            CompanyRecord companyRecord = new CompanyRecord(name, INN, address);
            loadedCompanyRecords.put(INN, companyRecord);
            if (sourceCsvPath != null) {
                int targetColumnCount = Math.max(this.columnCount, 5);
                List<String> columns = new ArrayList<>();
                for (int i = 0; i < targetColumnCount; i++) {
                    columns.add("");
                }
                columns.set(0, Integer.toString(nextRecordNumber));
                columns.set(1, name);
                columns.set(3, address);
                columns.set(4, INN);
                if (!CsvReader.appendRawCsvRow(sourceCsvPath, columns)) {
                    throw new CsvWriteException("Failed to persist company to CSV");
                }
                nextRecordNumber++;
                this.columnCount = targetColumnCount;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeCompanyRecordByINN(String INN)
            throws InvalidInnException, CompanyNotFoundException, CsvWriteException {
        validateINN(INN);
        lock.writeLock().lock();
        try {
            if (!loadedCompanyRecords.containsKey(INN)) {
                throw new CompanyNotFoundException("Company with a such INN wasn't found");
            }
            loadedCompanyRecords.remove(INN);
            if (sourceCsvPath != null) {
                Path path = Path.of(sourceCsvPath);
                try {
                    List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                    List<String> kept = new ArrayList<>();
                    for (String line : lines) {
                        String[] columns = line.split(";", -1);
                        if (columns.length > 4 && columns[4].trim().equals(INN)) {
                            continue;
                        }
                        kept.add(line);
                    }
                    Files.write(path, kept, StandardCharsets.UTF_8);
                } catch (Exception e) {
                    throw new CsvWriteException("Failed to persist CSV deletion", e);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}
