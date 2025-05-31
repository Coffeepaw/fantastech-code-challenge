package com.coffeepaw.smsapi.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequestDto {

    @NotBlank(message = "Sender must not be blank")
    @Pattern(regexp = "^\\+?\\d{7,15}$", message = "Sender phone number is invalid")
    private String from;

    @NotBlank(message = "Recipient must not be blank")
    @Pattern(regexp = "^\\+?\\d{7,15}$", message = "Recipient phone number is invalid")
    private String to;

    @NotBlank(message = "Message content must not be blank")
    private String message;
}