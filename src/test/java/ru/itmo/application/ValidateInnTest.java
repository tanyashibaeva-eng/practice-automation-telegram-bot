package ru.itmo.application;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import ru.itmo.domain.dto.command.InnValidationResult;
import ru.itmo.infra.client.NalogRuClient;
import ru.itmo.infra.storage.CachedInnRepository;
import ru.itmo.util.PropertiesProvider;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;


public class ValidateInnTest {
    @Test
    @DisplayName("Region = Санкт-Петербург -> isSPB = true, userShouldProvideCompanyRegion = false")
    void shouldMarkSpbCompanyCorrectly() throws Exception {
        String inn = "7843016840";

        try (MockedStatic<PropertiesProvider> propertiesProviderMock = mockStatic(PropertiesProvider.class);
             MockedStatic<NalogRuClient> nalogRuClientMock = mockStatic(NalogRuClient.class);
             MockedStatic<CachedInnRepository> cachedInnRepositoryMock = mockStatic(CachedInnRepository.class)) {

            propertiesProviderMock.when(PropertiesProvider::getInnCheck).thenReturn(true);
            nalogRuClientMock.when(() -> NalogRuClient.getCompanyInfoByInn(inn)).thenReturn(new String[]{"ANY", "Г.Санкт-Петербург"});
            cachedInnRepositoryMock.when(() -> CachedInnRepository.getOrFetchCompanyInfo(inn))
                    .thenReturn(new String[]{"ANY", "Г.Санкт-Петербург"});

            InnValidationResult result = StudentService.validateInn(inn);

            assertTrue(result.isSPB());
            assertFalse(result.isUserShouldProvideCompanyName());
        }
    }

    @Test
    @DisplayName("Region = Moscow -> isSPB = false, userShouldProvideCompanyRegion = false")
    void shouldMarkNonSpbCompanyCorrectly() throws Exception {
        String inn = "7706107510";

        try (MockedStatic<PropertiesProvider> propertiesProviderMock = mockStatic(PropertiesProvider.class);
             MockedStatic<NalogRuClient> nalogRuClientMock = mockStatic(NalogRuClient.class);
             MockedStatic<CachedInnRepository> cachedInnRepositoryMock = mockStatic(CachedInnRepository.class)) {

            propertiesProviderMock.when(PropertiesProvider::getInnCheck).thenReturn(true);
            nalogRuClientMock.when(() -> NalogRuClient.getCompanyInfoByInn(inn)).thenReturn(new String[]{"ANY", "Г.Москва"});
            cachedInnRepositoryMock.when(() -> CachedInnRepository.getOrFetchCompanyInfo(inn))
                    .thenReturn(new String[]{"ANY", "Г.Москва"});

            InnValidationResult result = StudentService.validateInn(inn);

            assertFalse(result.isSPB());
            assertFalse(result.isUserShouldProvideCompanyName());
        }
    }

    @Test
    @DisplayName("Region not found -> userShouldProvideCompanyRegion = true")
    void shouldRequestCompanyRegionWhenRegionNotFound() throws Exception {
        String inn = "1000000000";

        try (MockedStatic<PropertiesProvider> propertiesProviderMock = mockStatic(PropertiesProvider.class);
             MockedStatic<NalogRuClient> nalogRuClientMock = mockStatic(NalogRuClient.class);
             MockedStatic<CachedInnRepository> cachedInnRepositoryMock = mockStatic(CachedInnRepository.class)) {

            propertiesProviderMock.when(PropertiesProvider::getInnCheck).thenReturn(true);
            nalogRuClientMock.when(() -> NalogRuClient.getCompanyInfoByInn(inn)).thenReturn(new String[]{null, null});
            cachedInnRepositoryMock.when(() -> CachedInnRepository.getOrFetchCompanyInfo(inn))
                    .thenReturn(new String[]{null, null});

            InnValidationResult result = StudentService.validateInn(inn);

            assertTrue(result.isUserShouldProvideCompanyName());
        }
    }
}