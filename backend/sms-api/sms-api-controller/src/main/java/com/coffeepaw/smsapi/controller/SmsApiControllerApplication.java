package com.coffeepaw.smsapi.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.coffeepaw.smsapi",
        "com.coffeepaw.smsapi.controller",
        "com.coffeepaw.smsapi.service",
        "com.coffeepaw.smsapi.repository",
        "com.coffeepaw.smsapi.mapper",
        "com.coffeepaw.smsapi.model"
})
@EnableJpaRepositories(basePackages = "com.coffeepaw.smsapi.repository")
@EnableJpaAuditing
@EntityScan(basePackages = "com.coffeepaw.smsapi.model")
public class SmsApiControllerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmsApiControllerApplication.class, args);
    }
}