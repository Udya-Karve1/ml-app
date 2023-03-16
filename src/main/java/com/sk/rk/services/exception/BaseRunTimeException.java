package com.sk.rk.services.exception;

import lombok.Data;

@Data
public class BaseRunTimeException extends RuntimeException {

    private int errorCode;
    private String errorMessage;

    public BaseRunTimeException(String message) {
        super(message);
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
