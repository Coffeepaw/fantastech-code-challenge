package com.coffeepaw.smsapi.controller.web;

import com.coffeepaw.smsapi.model.dto.MessageDto;
import com.coffeepaw.smsapi.model.dto.SmsRequestDto;
import com.coffeepaw.smsapi.service.SmsHandlerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sms")
public class SmsApiController {

    private final SmsHandlerService smsHandlerService;

    @Autowired
    public SmsApiController(SmsHandlerService smsHandlerService) {
        this.smsHandlerService = smsHandlerService;
    }

    @PostMapping
    public ResponseEntity<MessageDto> sendSms(@Valid @RequestBody SmsRequestDto smsRequestDTO) {
        smsHandlerService.sendSms(smsRequestDTO);
        MessageDto response = MessageDto.builder()
                .message("SMS sent successfully.")
                .code(200)
                .build();

        return ResponseEntity.ok(response);
    }
}
