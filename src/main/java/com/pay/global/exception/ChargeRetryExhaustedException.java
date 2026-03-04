package com.pay.global.exception;

public class ChargeRetryExhaustedException extends RuntimeException {

    public ChargeRetryExhaustedException(String message) {
        super(message);
    }
}
