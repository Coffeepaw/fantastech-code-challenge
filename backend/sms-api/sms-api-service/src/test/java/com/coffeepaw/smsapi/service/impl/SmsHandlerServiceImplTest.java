package com.coffeepaw.smsapi.service.impl;

import com.coffeepaw.smsapi.model.Sms;
import com.coffeepaw.smsapi.model.SmsConfiguration;
import com.coffeepaw.smsapi.model.dto.SmsRequestDto;
import com.coffeepaw.smsapi.repository.SmsRepository;
import com.coffeepaw.smsapi.service.SmsConfigurationService;
import com.coffeepaw.smsapi.service.exception.DatabaseTransactionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SmsHandlerServiceImplTest {

    private static final int MAX_SMS_LENGTH = 160;
    private static final String SUFFIX_TEMPLATE = "... - Part %d of %d";

    @Mock
    private SmsRepository smsRepository;

    @Mock
    private SmsConfigurationService smsConfigurationService;

    @InjectMocks
    private SmsHandlerServiceImpl smsHandler;

    @Captor
    private ArgumentCaptor<Sms> smsCaptor;

    SmsRequestDto validDto = new SmsRequestDto();

    @BeforeEach
    void setUp() {
        SmsConfiguration config = new SmsConfiguration();
        config.setSuffixTemplate("... - Part %d of %d");
        config.setMaxSmsLength(160);

        when(smsConfigurationService.getMaxSmsLength()).thenReturn(160);
        when(smsConfigurationService.getSuffixTemplate()).thenReturn("... - Part %d of %d");

        smsHandler = new SmsHandlerServiceImpl(smsRepository, smsConfigurationService);

        validDto.setTo("+1234567890");
        validDto.setFrom("Service");
        validDto.setMessage("Hello, this is a test message that fits in one SMS.");
    }

    @Test
    void testSendSms_SinglePart() {
        String message = "Hello, this is short.";
        SmsRequestDto dto = new SmsRequestDto("123", "456", message);

        smsHandler.sendSms(dto);

        verify(smsRepository).save(smsCaptor.capture());
        Sms savedSms = smsCaptor.getValue();

        assertEquals("123", savedSms.getFrom());
        assertEquals("456", savedSms.getTo());
        assertEquals(message.length(), savedSms.getSize());
        assertEquals(1, savedSms.getParts());
        assertEquals(List.of(message), savedSms.getContent());
    }

    @Test
    void testSendSms_MultiPart() {
        String longMessage = "A".repeat(400);  // Should split into 3 parts
        SmsRequestDto dto = new SmsRequestDto("789", "101", longMessage);

        smsHandler.sendSms(dto);

        verify(smsRepository).save(smsCaptor.capture());
        Sms savedSms = smsCaptor.getValue();

        assertEquals("789", savedSms.getFrom());
        assertEquals("101", savedSms.getTo());
        assertEquals(longMessage.length(), savedSms.getSize());
        assertTrue(savedSms.getParts() > 1);
        assertEquals(savedSms.getParts(), savedSms.getContent().size());

        int totalLength = savedSms.getContent().stream().mapToInt(String::length).sum();
        assertTrue(totalLength >= longMessage.length());

        for (int i = 0; i < savedSms.getParts(); i++) {
            String part = savedSms.getContent().get(i);
            assertTrue(part.contains(String.format(smsConfigurationService.getSuffixTemplate(), i + 1, savedSms.getParts())));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("messageProvider")
    void testMessageSplitting(String description, String msg, int expectedParts) {
        SmsRequestDto dto = new SmsRequestDto("123", "456", msg);
        List<String> messages = invokeProcessMessage(dto);

        assertEquals(expectedParts, messages.size(), description);
        assertCombinedMessageEqualsOriginal(messages, msg);
        if (expectedParts > 1) {
            assertPartSuffixes(messages, expectedParts);
        }
    }

    @Test
    void testGenerateMessagesOnePart() {
        String shortMsg = "Hello World!";
        SmsRequestDto dto = new SmsRequestDto("123", "456", shortMsg);
        List<String> messages = invokeProcessMessage(dto);
        assertEquals(1, messages.size());
        assertEquals(shortMsg, messages.getFirst());
    }

    @Test
    void testGenerateMessagesMultipleParts() {
        String msg = "A".repeat(smsConfigurationService.getMaxSmsLength() + 1);
        List<String> messages = invokeGenerateMessages(msg, 2);
        assertEquals(2, messages.size());
        assertPartSuffixes(messages, 2);
        assertCombinedMessageEqualsOriginal(messages, msg);
    }

    @Test
    void shouldThrowDatabaseTransactionExceptionWhenSavingFails() {
        doThrow(new RuntimeException("DB error")).when(smsRepository).save(any(Sms.class));
        assertThrows(DatabaseTransactionException.class, () -> smsHandler.sendSms(validDto));
    }

    private static Stream<Arguments> messageProvider() {
        int suffix9 = String.format(SUFFIX_TEMPLATE, 9, 9).length();
        int suffix10 = String.format(SUFFIX_TEMPLATE, 10, 10).length();

        int perPart9 = MAX_SMS_LENGTH - suffix9;
        int perPart10 = MAX_SMS_LENGTH - suffix10;

        return Stream.of(
                Arguments.of("Single short message", "Hello!", 1),
                Arguments.of("Exactly two parts", "A".repeat(MAX_SMS_LENGTH + 1), 2),
                Arguments.of("Nine parts boundary", "A".repeat(perPart9 * 9), 9),
                Arguments.of("Crossing into ten parts", "A".repeat(perPart10 * 10), 10),
                Arguments.of("Very long message (2000 chars)", "A".repeat(2000), calculateExpectedParts("A".repeat(2000)))
        );
    }

    private static int calculateExpectedParts(String msg) {
        int length = msg.length();
        for (int parts = 1; parts < 100; parts++) {
            int suffixLength = String.format(SUFFIX_TEMPLATE, parts, parts).length();
            int charsPerPart = MAX_SMS_LENGTH - suffixLength;
            if (charsPerPart * parts >= length) {
                return parts;
            }
        }
        throw new IllegalArgumentException("Cannot determine parts for message of length " + length);
    }

    private List<String> invokeGenerateMessages(String msg, int parts) {
        try {
            var method = SmsHandlerServiceImpl.class.getDeclaredMethod("generateMessages", String.class, int.class);
            method.setAccessible(true);
            return (List<String>) method.invoke(smsHandler, msg, parts);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> invokeProcessMessage(SmsRequestDto dto) {
        try {
            var method = SmsHandlerServiceImpl.class.getDeclaredMethod("processMessage", SmsRequestDto.class);
            method.setAccessible(true);
            return (List<String>) method.invoke(smsHandler, dto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void assertCombinedMessageEqualsOriginal(List<String> messages, String original) {
        StringBuilder reconstructed = new StringBuilder();
        for (String msg : messages) {
            int suffixStart = msg.indexOf("... - Part");
            reconstructed.append(msg, 0, suffixStart != -1 ? suffixStart : msg.length());
        }
        assertEquals(original, reconstructed.toString());
    }

    private void assertPartSuffixes(List<String> messages, int totalParts) {
        for (int i = 0; i < totalParts; i++) {
            String expectedSuffix = String.format(smsConfigurationService.getSuffixTemplate(), i + 1, totalParts);
            assertTrue(messages.get(i).endsWith(expectedSuffix),
                    "Expected suffix not found: " + expectedSuffix);
        }
    }
}
