package com.spendcheck.exception;

public class CurrencyException extends RuntimeException {
    public CurrencyException(String message) {
        super(message);
    }

    public CurrencyException(String message, Throwable cause) {
        super(message, cause);
    }
}

