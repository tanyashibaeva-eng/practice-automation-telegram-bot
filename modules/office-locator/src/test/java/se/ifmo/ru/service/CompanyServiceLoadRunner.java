package se.ifmo.ru.service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CompanyServiceLoadRunner {
    public static void main(String[] args) throws Exception {
        int threads = args.length > 0 ? Integer.parseInt(args[0]) : 200;
        int opsPerThread = args.length > 1 ? Integer.parseInt(args[1]) : 100;
        int initialRecords = args.length > 2 ? Integer.parseInt(args[2]) : 20000;
        int addEvery = args.length > 3 ? Integer.parseInt(args[3]) : 2;

        Path tempDir = Files.createTempDirectory("company-load");
        Path csv = createSampleCsv(tempDir, initialRecords);
        CompanyService service = new CompanyService(csv.toString());

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger addCounter = new AtomicInteger(0);
        AtomicLong errorCount = new AtomicLong(0);

        long totalOps = (long) threads * opsPerThread;
        long startNs = System.nanoTime();

        for (int t = 0; t < threads; t++) {
            executor.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < opsPerThread; i++) {
                        try {
                            if (addEvery > 0 && i % addEvery == 0) {
                                int value = 900000000 + addCounter.incrementAndGet();
                                String inn = String.format("%010d", value);
                                service.addCompanyRecord("Load Company " + inn, inn, "Load Address " + inn);
                            } else {
                                int idx = (i % initialRecords) + 1;
                                String inn = String.format("%010d", 770000000 + idx);
                                service.findCompanyRecordByINN(inn);
                            }
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        start.countDown();
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.MINUTES);

        long durationNs = System.nanoTime() - startNs;
        double seconds = durationNs / 1_000_000_000.0;
        double opsPerSec = totalOps / seconds;

        System.out.println("Threads: " + threads);
        System.out.println("Ops per thread: " + opsPerThread);
        System.out.println("Initial records: " + initialRecords);
        System.out.println("Add every N ops: " + addEvery);
        System.out.println("Total ops: " + totalOps);
        System.out.println("Duration (s): " + String.format("%.3f", seconds));
        System.out.println("Throughput (ops/s): " + String.format("%.2f", opsPerSec));
        System.out.println("Errors: " + errorCount.get());
        System.out.println("CSV file: " + csv);
    }

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
}
