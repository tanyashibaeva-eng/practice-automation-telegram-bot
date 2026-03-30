package se.ifmo.ru.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompanyServiceConcurrencyTest {
    private static final int THREADS = 200;
    private static final int INITIAL_RECORDS_FOR_FIND = 20000;
    private static final int ITERATIONS_PER_THREAD = 100;
    private static final int THREADS_FOR_ADD = 200;
    private static final int INITIAL_RECORDS_FOR_ADD = 20000;
    private static final int ADDS_PER_THREAD = 2;

    private static Path createSampleCsv(Path dir, int records) throws Exception {
        Path path = dir.resolve("companies.csv");
        List<String> lines = new ArrayList<>();
        lines.add("No;Name;RegNumber;Address;TaxId;StatCode;Region;Activity;2024");
        int baseInn = 770000000;
        for (int i = 1; i <= records; i++) {
            String inn = String.format("%010d", baseInn + i);
            lines.add(i + ";Company " + i + ";REG" + i + ";Address " + i + ";" + inn + ";111;Region;IT;10");
        }
        Files.write(path, lines, StandardCharsets.UTF_8);
        return path;
    }

    @Test
    void concurrentFinds_doNotThrow(@TempDir Path tempDir) throws Exception {
        Path csv = createSampleCsv(tempDir, INITIAL_RECORDS_FOR_FIND);
        CompanyService service = new CompanyService(csv.toString());

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();

        for (int t = 0; t < THREADS; t++) {
            executor.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < ITERATIONS_PER_THREAD; i++) {
                        int idx = (i % INITIAL_RECORDS_FOR_FIND) + 1;
                        String inn = String.format("%010d", 770000000 + idx);
                        service.findCompanyRecordByINN(inn);
                    }
                } catch (Throwable e) {
                    errors.add(e);
                }
            });
        }

        start.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        assertTrue(errors.isEmpty(), "Unexpected errors: " + errors);
    }

    @Test
    void concurrentAdds_areThreadSafe(@TempDir Path tempDir) throws Exception {
        Path csv = createSampleCsv(tempDir, INITIAL_RECORDS_FOR_ADD);
        CompanyService service = new CompanyService(csv.toString());

        ExecutorService executor = Executors.newFixedThreadPool(THREADS_FOR_ADD);
        CountDownLatch start = new CountDownLatch(1);
        ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();
        AtomicInteger counter = new AtomicInteger(0);
        Set<String> addedInns = ConcurrentHashMap.newKeySet();

        for (int t = 0; t < THREADS_FOR_ADD; t++) {
            executor.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < ADDS_PER_THREAD; i++) {
                        int value = 800000000 + counter.incrementAndGet();
                        String inn = String.format("%010d", value);
                        service.addCompanyRecord("New Company " + inn, inn, "Address " + inn);
                        addedInns.add(inn);
                    }
                } catch (Throwable e) {
                    errors.add(e);
                }
            });
        }

        start.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        assertTrue(errors.isEmpty(), "Unexpected errors: " + errors);

        List<String> lines = Files.readAllLines(csv, StandardCharsets.UTF_8);
        Set<String> innsFromFile = ConcurrentHashMap.newKeySet();
        for (int i = 1; i < lines.size(); i++) {
            String[] columns = lines.get(i).split(";", -1);
            if (columns.length > 4) {
                innsFromFile.add(columns[4].trim());
            }
        }

        assertTrue(innsFromFile.containsAll(addedInns));
        assertEquals(1 + INITIAL_RECORDS_FOR_ADD + THREADS_FOR_ADD * ADDS_PER_THREAD, lines.size());
    }
}
