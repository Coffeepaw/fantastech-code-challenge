package com.coffeepaw.smsapi.repository;

import com.coffeepaw.smsapi.model.SmsConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SmsConfigurationRepository extends JpaRepository<SmsConfiguration, Long> {
    Optional<SmsConfiguration> findTopByOrderByCreatedAtDesc();
}