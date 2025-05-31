package com.coffeepaw.smsapi.repository;

import com.coffeepaw.smsapi.model.Sms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsRepository extends JpaRepository<Sms, Long> {
}
