package com.coffeepaw.smsapi.controller.web;

import com.coffeepaw.smsapi.mapper.SmsConfigurationMapper;
import com.coffeepaw.smsapi.model.SmsConfiguration;
import com.coffeepaw.smsapi.model.dto.MessageDto;
import com.coffeepaw.smsapi.model.dto.SmsConfigurationDto;
import com.coffeepaw.smsapi.service.SmsConfigurationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sms/configuration")
public class SmsConfigurationApiController {

    private final SmsConfigurationService smsConfigurationService;

    @Autowired
    public SmsConfigurationApiController(SmsConfigurationService smsConfigurationService) {
        this.smsConfigurationService = smsConfigurationService;
    }


    @PostMapping
    public ResponseEntity<MessageDto> addSmsConfiguration(@Valid @RequestBody SmsConfigurationDto smsConfigurationDto) {
        smsConfigurationService.createNewConfiguration(smsConfigurationDto);
        MessageDto response = MessageDto.builder()
                .message("New configuration added.")
                .code(200)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<SmsConfigurationDto> getCurrentSmsConfiguration() {
        SmsConfiguration configuration = smsConfigurationService.getCurrentConfiguration();
        SmsConfigurationDto dto = SmsConfigurationMapper.INSTANCE.toDto(configuration);
        return ResponseEntity.ok(dto);
    }
}
