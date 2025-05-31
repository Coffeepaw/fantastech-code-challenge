package com.coffeepaw.smsapi.model;

import jakarta.persistence.*;
import lombok.*;


import java.time.Instant;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sms_configuration")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SmsConfiguration extends AuditModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "max_sms_length", nullable = false)
    private int maxSmsLength;

    @Column(name = "suffix_template", nullable = false)
    private String suffixTemplate;

}