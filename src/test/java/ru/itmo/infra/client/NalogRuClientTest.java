package ru.itmo.infra.client;

import org.junit.jupiter.api.Test;
import ru.itmo.exception.DefunctCompanyException;
import ru.itmo.exception.InvalidCompanyRegistrationException;

import static org.junit.jupiter.api.Assertions.*;

class NalogRuClientTest {

    private static final String[] SPB_INNS = {
            "7707049388",
            "4703105075",
            "7843016840"
    };

    private static final String[][] NON_SPB_INNS = {
            {"9705031526", "Г.Москва"},
            {"9102205050", "Алтайский край"},
            {"3663075849", "Воронежская область"},
            {"3234016700", "Брянская область"}

    };

    private static final String[] INVALID_INNS = {
            "0101010101",
            "1234567890",
            "6767676767"
    };

    private static final String[] DEFUNCT_COMPANY_INNS = {
            "6323075389",
            "7830001028",
            "9204002242"
    };

    private static final String[] INVALID_COMPANY_REGISTRATION_INNS = {
            "0000000000"
    };

    @Test
    void shouldReturnSpbRegions() throws Exception {
        for (String inn : SPB_INNS) {
            String region = NalogRuClient.getCompanyInfoByInn(inn)[1];

            assertNotNull(region, "Регион не должен быть null для инн: " + inn);
            assertEquals("Г.Санкт-Петербург", region,
                    "Ожидается Г.Санкт-Петербург для инн: " + inn);
        }
    }

    @Test
    void shouldReturnCorrectRegionForNonSpbRegions() throws Exception {
        for (String[] innWithRegion : NON_SPB_INNS) {
            String inn = innWithRegion[0];
            String expectedRegion = innWithRegion[1];

            String actualRegion = NalogRuClient.getCompanyInfoByInn(inn)[1];

            assertNotNull(actualRegion, "Регион не должен быть null для инн: " + inn);
            assertEquals(expectedRegion, actualRegion,
                    "Неверный регион для инн: " + inn + ". Ожидался регион: " + expectedRegion);
        }
    }

    @Test
    void shouldReturnNullForInvalidInn() throws Exception {
        for (String inn : INVALID_INNS) {
            String region = NalogRuClient.getCompanyInfoByInn(inn)[1];

            assertNull(region, "Регион должен быть null для инн: " + inn);
        }
    }


    @Test
    void shouldThrowDefunctCompanyExceptionForDefunctInns() {
        for (String inn : DEFUNCT_COMPANY_INNS) {
            assertThrows(
                    DefunctCompanyException.class,
                    () -> NalogRuClient.getCompanyInfoByInn(inn),
                    "Ожидалась DefunctCompanyException для ИНН: " + inn
            );
        }
    }

    @Test
    void shouldThrowInvalidCompanyRegistrationExceptionForInvalidCompanyRegistrationInns() {
        for (String inn : INVALID_COMPANY_REGISTRATION_INNS) {
            assertThrows(
                    InvalidCompanyRegistrationException.class,
                    () -> NalogRuClient.getCompanyInfoByInn(inn),
                    "Ожидалась InvalidCompanyRegistrationException для ИНН: " + inn
            );
        }
    }
}