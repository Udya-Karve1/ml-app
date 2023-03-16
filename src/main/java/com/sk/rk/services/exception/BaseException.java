package com.sk.rk.services.exception;

import lombok.Data;

/**
 * A common exception implementation.
 *
 *
 * @author uday.karve
 *
 * Base class for exception handling
 */
@Data
public class BaseException extends Exception {
    private final int errorCode;
    private final String errorMessage;

    public BaseException(int errorCode, String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public BaseException(int errorCode, String errorMessage, Throwable throwable) {
        super(errorMessage, throwable);
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }
}
