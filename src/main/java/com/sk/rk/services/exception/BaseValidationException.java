package com.sk.rk.services.exception;

import java.util.List;

public class BaseValidationException extends RuntimeException {

    private List<String> messages;

    public BaseValidationException() {
        super();
    }

    public BaseValidationException(List<String> messages) {
        super();
        this.messages = messages;
    }

    public List<String> getMessages () {
        return messages;
    }
}
