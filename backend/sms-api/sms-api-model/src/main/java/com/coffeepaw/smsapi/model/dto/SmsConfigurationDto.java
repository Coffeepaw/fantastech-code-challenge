package com.coffeepaw.smsapi.model.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SmsConfigurationDto {

    @Min(value = 1, message = "Max SMS length must be at least 1")
    private int maxSmsLength;

    @NotBlank(message = "Suffix template cannot be blank")
    private String suffixTemplate;
}
