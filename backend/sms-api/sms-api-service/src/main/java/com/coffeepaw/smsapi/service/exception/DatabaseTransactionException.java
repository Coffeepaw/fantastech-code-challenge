package com.coffeepaw.smsapi.service.exception;

public class DatabaseTransactionException extends RuntimeException {
    public DatabaseTransactionException(String message, Throwable cause) {
        super(message, cause);
    }

}
