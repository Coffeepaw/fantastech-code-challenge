package com.coffeepaw.smsapi.service.impl;

import com.coffeepaw.smsapi.model.SmsConfiguration;
import com.coffeepaw.smsapi.model.dto.SmsConfigurationDto;
import com.coffeepaw.smsapi.repository.SmsConfigurationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SmsConfigurationServiceImplTest {

    private SmsConfigurationRepository configurationRepository;
    private SmsConfigurationServiceImpl service;

    @BeforeEach
    void setUp() {
        configurationRepository = mock(SmsConfigurationRepository.class);
        service = new SmsConfigurationServiceImpl(configurationRepository);
    }

    @Test
    void getCurrentConfiguration_shouldReturnConfiguration() {
        SmsConfiguration mockConfig = SmsConfiguration.builder()
                .suffixTemplate("... - Part %d of %d")
                .maxSmsLength(160)
                .build();

        when(configurationRepository.findTopByOrderByCreatedAtDesc()).thenReturn(Optional.of(mockConfig));

        SmsConfiguration result = service.getCurrentConfiguration();

        assertNotNull(result);
        assertEquals("... - Part %d of %d", result.getSuffixTemplate());
        assertEquals(160, result.getMaxSmsLength());
    }

    @Test
    void getCurrentConfiguration_shouldThrowExceptionWhenEmpty() {
        when(configurationRepository.findTopByOrderByCreatedAtDesc()).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            service.getCurrentConfiguration();
        });

        assertEquals("No SMS configuration found", ex.getMessage());
    }

    @Test
    void checkConfiguration_shouldLoadConfigurationOnlyOnce() {
        SmsConfiguration mockConfig = SmsConfiguration.builder()
                .suffixTemplate("[Part %d of %d]")
                .maxSmsLength(140)
                .build();

        when(configurationRepository.findTopByOrderByCreatedAtDesc()).thenReturn(Optional.of(mockConfig));

        service.checkConfiguration();

        assertEquals(140, service.getMaxSmsLength());
        assertEquals("[Part %d of %d]", service.getSuffixTemplate());

        service.checkConfiguration();

        verify(configurationRepository, times(3)).findTopByOrderByCreatedAtDesc();
    }

    @Test
    void createNewConfiguration_shouldSaveAndLoadConfiguration() {
        SmsConfigurationDto dto = new SmsConfigurationDto(150, "[x/y]");

        service.createNewConfiguration(dto);

        ArgumentCaptor<SmsConfiguration> captor = ArgumentCaptor.forClass(SmsConfiguration.class);
        verify(configurationRepository, times(1)).save(captor.capture());

        SmsConfiguration saved = captor.getValue();
        assertEquals(150, saved.getMaxSmsLength());
        assertEquals("[x/y]", saved.getSuffixTemplate());

        assertEquals(150, service.getMaxSmsLength());
        assertEquals("[x/y]", service.getSuffixTemplate());
    }
}
