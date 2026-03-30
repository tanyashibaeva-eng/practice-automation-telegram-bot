package ru.itmo.infra.storage;

import lombok.extern.java.Log;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.client.NalogRuClient;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Репозиторий данных о компании (ИНН, название, регион регистрации)
 */
@Log
public class CachedInnRepository {

    private static final Connection connection = DatabaseManager.getConnection();

    public static String findRegionByInn(String inn) throws InternalException {
        try (var statement = connection.prepareStatement("""
                SELECT region
                FROM cached_inn
                WHERE company_inn = ?;
                """
        )) {
            statement.setString(1, inn);
            var rs = statement.executeQuery();

            if (rs.next()) {
                return rs.getString("region");
            }
            return null;

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static String findNameByInn(String inn) throws InternalException {
        try (var statement = connection.prepareStatement("""
                SELECT name
                FROM cached_inn
                WHERE company_inn = ?;
                """
        )) {
            statement.setString(1, inn);
            var rs = statement.executeQuery();

            if (rs.next()) {
                return rs.getString("name");
            }
            return null;

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    public static void saveInn(String inn, String name, String region) throws InternalException {
        try (var statement = connection.prepareStatement("""
                INSERT INTO cached_inn (company_inn, name, region, cached_at)
                VALUES (?, ?, ?, now())
                """
        )) {
            statement.setString(1, inn);
            statement.setString(2, name);
            statement.setString(3, region);
            statement.executeUpdate();

        } catch (SQLException ex) {
            throw handleAndWrapSQLException(ex);
        }
    }

    /**
     * Метод для получения данных о компании. Данные получается через NalogRuClient и кэшируются локально
     * @return Массив из названия и региона регистрации компании если удалось найти данные, иначе массив из null
     */
    public static String[] getOrFetchCompanyInfo(String inn) throws InternalException, IOException {
        String cachedName = findNameByInn(inn);
        String cachedRegion = findRegionByInn(inn);

        if (cachedRegion != null) {
            return new String[]{cachedName, cachedRegion};
        }

        String[] companyInfo = NalogRuClient.getCompanyInfoByInn(inn);
        String name = companyInfo[0];
        String region = companyInfo[1];
        if (region == null) {
            return new String[]{null, null};
        }
        saveInn(inn, name, region);

        return companyInfo;
    }

    private static InternalException handleAndWrapSQLException(SQLException ex) {
        log.severe("Ошибка во время выполнения SQL запроса: " + ex.getMessage());
        return new InternalException("Что-то пошло не так", ex.getCause());
    }
}