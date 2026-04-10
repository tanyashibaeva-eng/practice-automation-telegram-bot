package ru.itmo.infra.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import ru.itmo.infra.client.NalogRuClient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mockStatic;

class CachedInnRepositoryTest {

    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        connection = DatabaseManager.getConnection();

        try (Statement st = connection.createStatement()) {
            st.executeUpdate("DELETE FROM cached_inn");
        }
    }

    @Test
    void shouldFetchRegionAndSaveToCache() throws Exception {
        String inn = "7843016840";
        String name = "ITMO";
        String region = "Г.Санкт-Петербург";

        try (MockedStatic<NalogRuClient> nalogMock = mockStatic(NalogRuClient.class)) {

            nalogMock.when(() -> NalogRuClient.getCompanyInfoByInn(inn))
                    .thenReturn(new String[]{name, region});

            String[] result = CachedInnRepository.getOrFetchCompanyInfo(inn);

            assertEquals(name, result[0]);
            assertEquals(region, result[1]);
            assertEquals(1, countRows(inn));
            assertEquals(region, getRegion(inn));
            assertEquals(name, getName(inn));
        }
    }

    @Test
    void shouldReturnNullAndNotSaveWhenCompanyNotFound() throws Exception {
        String inn = "1000000000";

        try (MockedStatic<NalogRuClient> nalogMock = mockStatic(NalogRuClient.class)) {

            nalogMock.when(() -> NalogRuClient.getCompanyInfoByInn(inn))
                    .thenReturn(new String[]{null, null});

            String[] result = CachedInnRepository.getOrFetchCompanyInfo(inn);

            assertNull(result[0]);
            assertNull(result[1]);

            assertEquals(0, countRows(inn));
        }
    }

    @Test
    void shouldReturnCachedValueWithoutCallingExternalService() throws Exception {
        String inn = "7706107510";
        String name = "MSU";
        String region = "Г.Москва";

        insert(inn, name, region);

        try (MockedStatic<NalogRuClient> nalogMock = mockStatic(NalogRuClient.class)) {

            String[] result = CachedInnRepository.getOrFetchCompanyInfo(inn);

            assertEquals(name, result[0]);
            assertEquals(region, result[1]);

            nalogMock.verifyNoInteractions();
        }
    }

    private void insert(String inn,String name, String region) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO cached_inn (company_inn, name, region, cached_at)
                VALUES (?, ?, ?, now())
        """)) {
            ps.setString(1, inn);
            ps.setString(2, name);
            ps.setString(3, region);
            ps.executeUpdate();
        }
    }

    private int countRows(String inn) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("""
                SELECT count(*) FROM cached_inn WHERE company_inn = ?
        """)) {
            ps.setString(1, inn);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private String getRegion(String inn) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("""
                SELECT region FROM cached_inn WHERE company_inn = ?
        """)) {
            ps.setString(1, inn);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("region") : null;
            }
        }
    }

    private String getName(String inn) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("""
                SELECT name FROM cached_inn WHERE company_inn = ?
        """)) {
            ps.setString(1, inn);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("name") : null;
            }
        }
    }
}