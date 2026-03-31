package ru.itmo.infra.excel;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GoogleSheetsExporterTest {
    @Test
    public void testCheckInnInExcel_ValidInn_ReturnsTrue() throws IOException {
        boolean result = GoogleSheetsExporter.checkInnInCsv(7801019126L);
        assertTrue(result, "ИНН должен быть найден");
    }

    @Test
    public void testCheckInnInExcel_ValidInn_ReturnsFalse() throws IOException {
        boolean result = GoogleSheetsExporter.checkInnInCsv(17801019126L);
        assertFalse(result, "ИНН не должен быть найден");
    }
}
