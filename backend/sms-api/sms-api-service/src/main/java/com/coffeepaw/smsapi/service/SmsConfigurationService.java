package com.coffeepaw.smsapi.service;

import com.coffeepaw.smsapi.model.SmsConfiguration;
import com.coffeepaw.smsapi.model.dto.SmsConfigurationDto;

public interface SmsConfigurationService {
    SmsConfiguration getCurrentConfiguration();

    void checkConfiguration();

    int getMaxSmsLength();

    String getSuffixTemplate();

    void createNewConfiguration(SmsConfigurationDto smsConfigurationDto);
}