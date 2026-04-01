package ru.itmo.application;

import lombok.extern.java.Log;
import ru.itmo.domain.model.CompanyApprovalRequest;
import ru.itmo.exception.InternalException;
import se.ifmo.ru.exception.CompanyAlreadyExistsException;
import se.ifmo.ru.exception.CompanyNotFoundException;
import se.ifmo.ru.exception.CompanyServiceException;
import se.ifmo.ru.exception.CsvReadException;
import se.ifmo.ru.exception.CsvWriteException;
import se.ifmo.ru.exception.InvalidCompanyDataException;
import se.ifmo.ru.exception.InvalidInnException;
import se.ifmo.ru.parser.CompanyRecord;
import se.ifmo.ru.service.CompanyService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Log
public class ApprovedCompanyRegistryService {

    private static final String CSV_PATH_ENV = "OFFICE_LOCATOR_CSV_PATH";
    private static final String CSV_PATH_PROPERTY = "office.locator.csv.path";
    private static final List<Path> DEFAULT_CSV_PATHS = List.of(
            Path.of("modules", "office-locator", "src", "main", "resources", "SPARK_IT.csv"),
            Path.of("office-locator", "src", "main", "resources", "SPARK_IT.csv")
    );

    private static final Object LOCK = new Object();

    private static volatile CompanyService companyService;
    private static volatile Path loadedCsvPath;
    private static volatile Path overriddenCsvPath;

    private ApprovedCompanyRegistryService() {
    }

    public static boolean hasOfficeInSaintPetersburg(long inn) throws InternalException {
        return findCompanyRecord(inn) != null;
    }

    public static String getCompanyName(long inn) throws InternalException {
        CompanyRecord companyRecord = findCompanyRecord(inn);
        return companyRecord == null ? null : companyRecord.getName();
    }

    public static String getCompanyAddress(long inn) throws InternalException {
        CompanyRecord companyRecord = findCompanyRecord(inn);
        return companyRecord == null ? null : companyRecord.getAddress();
    }

    public static void saveApprovedCompany(CompanyApprovalRequest request) throws InternalException {
        if (!request.isRequiresSpbOfficeApproval()) {
            return;
        }

        try {
            getCompanyService().addCompanyRecord(
                    request.getCompanyName(),
                    Long.toString(request.getInn()),
                    request.getCompanyAddress()
            );
        } catch (CompanyAlreadyExistsException ignored) {
            log.info("Company with INN " + request.getInn() + " is already present in Saint Petersburg registry");
        } catch (InvalidInnException | InvalidCompanyDataException | CsvWriteException | CsvReadException e) {
            log.severe("Failed to persist approved Saint Petersburg office: " + e.getMessage());
            throw new InternalException("Не удалось обновить CSV с офисами компаний в Санкт-Петербурге", e);
        }
    }

    static void overrideCsvPathForTests(Path csvPath) {
        synchronized (LOCK) {
            overriddenCsvPath = csvPath;
            companyService = null;
            loadedCsvPath = null;
        }
    }

    static void resetForTests() {
        overrideCsvPathForTests(null);
    }

    private static CompanyService getCompanyService() throws CsvReadException {
        Path csvPath = resolveCsvPath();
        CompanyService cachedService = companyService;
        if (cachedService != null && csvPath.equals(loadedCsvPath)) {
            return cachedService;
        }

        synchronized (LOCK) {
            if (companyService == null || !csvPath.equals(loadedCsvPath)) {
                log.info("Loading Saint Petersburg office registry into memory from " + csvPath);
                companyService = new CompanyService(csvPath.toString());
                loadedCsvPath = csvPath;
                log.info("Saint Petersburg office registry cached in memory");
            }
            return companyService;
        }
    }

    private static CompanyRecord findCompanyRecord(long inn) throws InternalException {
        try {
            return getCompanyService().findCompanyRecordByINN(Long.toString(inn));
        } catch (CompanyNotFoundException e) {
            return null;
        } catch (CompanyServiceException e) {
            log.severe("Failed to read company data from Saint Petersburg registry: " + e.getMessage());
            throw new InternalException("Не удалось получить данные компании из реестра офисов в Санкт-Петербурге", e);
        }
    }

    private static Path resolveCsvPath() {
        if (overriddenCsvPath != null) {
            return overriddenCsvPath;
        }

        String envPath = System.getenv(CSV_PATH_ENV);
        if (envPath != null && !envPath.isBlank()) {
            return Path.of(envPath).toAbsolutePath().normalize();
        }

        String propertyPath = System.getProperty(CSV_PATH_PROPERTY);
        if (propertyPath != null && !propertyPath.isBlank()) {
            return Path.of(propertyPath).toAbsolutePath().normalize();
        }

        for (Path candidate : DEFAULT_CSV_PATHS) {
            Path absoluteCandidate = candidate.toAbsolutePath().normalize();
            if (Files.exists(absoluteCandidate)) {
                return absoluteCandidate;
            }
        }

        return DEFAULT_CSV_PATHS.get(0).toAbsolutePath().normalize();
    }
}
