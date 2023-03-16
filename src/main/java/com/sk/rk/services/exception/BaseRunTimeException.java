package com.sk.rk.services.exception;

import lombok.Data;

@Data
public class BaseRunTimeException extends RuntimeException {

    private final int errorCode;
    private final String errorMessage;

    public BaseRunTimeException(String message) {
        super(message);
        this.errorCode = 400;
        this.errorMessage = message;
    }

    public BaseRunTimeException(int errorCode, String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public BaseRunTimeException(int errorCode, String errorMessage, Throwable throwable) {
        super(errorMessage, throwable);
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }
}
