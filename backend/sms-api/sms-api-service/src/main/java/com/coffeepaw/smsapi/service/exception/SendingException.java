package com.coffeepaw.smsapi.service.exception;

public class SendingException extends RuntimeException {
    public SendingException(String message, Throwable cause) {
        super(message, cause);
    }

}
