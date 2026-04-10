package ru.itmo.infra.storage;

import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import ru.itmo.infra.client.NalogRuClient;

import java.sql.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
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
        String region = "Г.Санкт-Петербург";

        try (MockedStatic<NalogRuClient> nalogMock = mockStatic(NalogRuClient.class)) {

            nalogMock.when(() -> Arrays.stream(NalogRuClient.getCompanyInfoByInn(inn)).toList().get(0))
                    .thenReturn(region);

            String result = CachedInnRepository.getOrFetchCompanyInfo(inn)[0];

            assertEquals(region, result);
            assertEquals(1, countRows(inn));
            assertEquals(region, getRegion(inn));
        }
    }

    @Test
    void shouldReturnNullAndNotSaveWhenCompanyNotFound() throws Exception {
        String inn = "1000000000";

        try (MockedStatic<NalogRuClient> nalogMock = mockStatic(NalogRuClient.class)) {

            nalogMock.when(() -> Arrays.stream(NalogRuClient.getCompanyInfoByInn(inn)).toList().get(0))
                    .thenReturn(null);

            String result = CachedInnRepository.getOrFetchCompanyInfo(inn)[0];

            assertNull(result);
            assertEquals(0, countRows(inn));
        }
    }

    @Test
    void shouldReturnCachedValueWithoutCallingExternalService() throws Exception {
        String inn = "7706107510";
        String region = "Г.Москва";

        insert(inn, region);

        try (MockedStatic<NalogRuClient> nalogMock = mockStatic(NalogRuClient.class)) {

            String result = CachedInnRepository.getOrFetchCompanyInfo(inn)[0];

            assertEquals(region, result);

            nalogMock.verifyNoInteractions();
        }
    }

    private void insert(String inn, String region) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO cached_inn (company_inn, region, cached_at)
                VALUES (?, ?, now())
        """)) {
            ps.setString(1, inn);
            ps.setString(2, region);
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
}