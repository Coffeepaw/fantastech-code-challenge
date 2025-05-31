package com.coffeepaw.smsapi.service.impl;

import com.coffeepaw.smsapi.model.Sms;
import com.coffeepaw.smsapi.model.dto.SmsRequestDto;
import com.coffeepaw.smsapi.repository.SmsRepository;
import com.coffeepaw.smsapi.service.SmsConfigurationService;
import com.coffeepaw.smsapi.service.SmsHandlerService;
import com.coffeepaw.smsapi.service.exception.DatabaseTransactionException;
import com.coffeepaw.smsapi.service.exception.SendingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SmsHandlerServiceImpl implements SmsHandlerService {

    private static final Logger logger = LoggerFactory.getLogger(SmsHandlerServiceImpl.class);

    private final SmsRepository smsRepository;
    private final SmsConfigurationService smsConfigurationService;


    @Autowired
    public SmsHandlerServiceImpl(SmsRepository smsRepository, SmsConfigurationService smsConfigurationService) {
        this.smsRepository = smsRepository;
        this.smsConfigurationService = smsConfigurationService;
    }

    @Override
    public void sendSms(SmsRequestDto smsRequestDTO) {
        logger.debug("Sending SMS to {} from {}", smsRequestDTO.getTo(), smsRequestDTO.getFrom());
        smsConfigurationService.checkConfiguration();
        List<String> messages = processMessage(smsRequestDTO);
        Sms sms = Sms.builder()
                .to(smsRequestDTO.getTo())
                .from(smsRequestDTO.getFrom())
                .size(smsRequestDTO.getMessage().length())
                .parts(messages.size())
                .sentDate(LocalDateTime.now())
                .content(messages)
                .build();

        saveSms(sms);
        logger.debug("Sms: {}", sms);
        logger.info("Sending SMS to {}...", sms.getTo());
        sendSmsToRecipient(sms);
        logger.info("SMS Sent to {}", sms.getTo());
    }

    private void sendSmsToRecipient(Sms sms) {
        try {
            sms.getContent().forEach(System.out::println);
        } catch (Exception e) {
            throw new SendingException("Error while sending SMS", e);
        }
    }

    private void saveSms(Sms sms) {
        try {
            logger.debug("Saving SMS");
            smsRepository.save(sms);
            logger.debug("SMS saved successfully");
        } catch (Exception e) {
            throw new DatabaseTransactionException("Error while saving SMS", e);
        }
    }

    private List<String> processMessage(SmsRequestDto sms) {
        logger.debug("Calculating characteristics for {}", sms.getTo());
        int totalLength = sms.getMessage().length();

        if (totalLength <= smsConfigurationService.getMaxSmsLength()) {
            return List.of(sms.getMessage());
        }

        int parts = 2;
        while (true) {
            List<String> messages = generateMessages(sms.getMessage(), parts);

            int coveredLength = 0;
            for (int i = 0; i < messages.size(); i++) {
                String suffix = createSuffix(i + 1, parts);
                coveredLength += messages.get(i).length() - suffix.length();
            }

            if (coveredLength >= totalLength) {
                return messages;
            }

            parts++;
        }
    }

    private List<String> generateMessages(String text, int parts) {
        logger.debug("Generating Messages To Send");
        List<String> messages = new ArrayList<>();

        int start = 0;
        for (int i = 1; i <= parts; i++) {
            String suffix = createSuffix(i, parts);
            int partLength = smsConfigurationService.getMaxSmsLength() - suffix.length();
            if (partLength <= 0) {
                throw new IllegalArgumentException("Suffix too long to fit in a single SMS part.");
            }
            int end = Math.min(start + partLength, text.length());
            messages.add(new StringBuilder(text.substring(start, end)).append(suffix).toString());
            start = end;
        }

        return messages;
    }

    private String createSuffix(int partNumber, int totalParts) {
        return String.format(smsConfigurationService.getSuffixTemplate(), partNumber, totalParts);
    }

}
