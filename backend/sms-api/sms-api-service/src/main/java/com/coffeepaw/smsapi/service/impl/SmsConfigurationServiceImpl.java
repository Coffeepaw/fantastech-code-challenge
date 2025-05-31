package com.coffeepaw.smsapi.service.impl;

import com.coffeepaw.smsapi.model.SmsConfiguration;
import com.coffeepaw.smsapi.model.dto.SmsConfigurationDto;
import com.coffeepaw.smsapi.repository.SmsConfigurationRepository;
import com.coffeepaw.smsapi.service.SmsConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SmsConfigurationServiceImpl implements SmsConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(SmsConfigurationServiceImpl.class);
    private final SmsConfigurationRepository configurationRepository;

    private int maxSmsLength = -1;
    private String suffixTemplate = null;


    @Autowired
    public SmsConfigurationServiceImpl(SmsConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    @Override
    public SmsConfiguration getCurrentConfiguration() {
        return configurationRepository.findTopByOrderByCreatedAtDesc()
                .orElseThrow(() -> new IllegalStateException("No SMS configuration found"));
    }

    @Override
    public void checkConfiguration() {
        logger.debug("Checking SMS configuration");
        if (suffixTemplate == null || maxSmsLength == -1) {
            logger.debug("SMS configuration is not loaded");
            SmsConfiguration smsConfiguration = getCurrentConfiguration();
            logger.debug("loading SMS configuration...");
            suffixTemplate = smsConfiguration.getSuffixTemplate();
            maxSmsLength = smsConfiguration.getMaxSmsLength();
            logger.debug("SMS configuration loaded");
        }
        logger.debug("Configuration: {}", getCurrentConfiguration());
    }

    @Override
    public int getMaxSmsLength() {
        return maxSmsLength;
    }

    @Override
    public String getSuffixTemplate() {
        return suffixTemplate;
    }

    @Override
    public void createNewConfiguration(SmsConfigurationDto configurationDto) {
        logger.debug("Creating new SMS configuration");
        SmsConfiguration configuration = SmsConfiguration.builder()
                .maxSmsLength(configurationDto.getMaxSmsLength())
                .suffixTemplate(configurationDto.getSuffixTemplate())
                .build();
        configurationRepository.save(configuration);
        logger.debug("New configuration created: {}", configuration);
        maxSmsLength = configuration.getMaxSmsLength();
        suffixTemplate = configurationDto.getSuffixTemplate();
        logger.debug("New configuration loaded");
    }
}