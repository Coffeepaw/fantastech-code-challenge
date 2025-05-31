package com.coffeepaw.smsapi.service;

import com.coffeepaw.smsapi.model.dto.SmsRequestDto;

public interface SmsHandlerService {
    void sendSms(SmsRequestDto smsRequestDTO);
}
