package com.sk.rk.services.exception;

import java.util.ArrayList;
import java.util.List;

public class BaseValidationException extends RuntimeException {

    private final List<String> messages;

    public BaseValidationException() {
        super();
        this.messages = new ArrayList<>();
    }

    public BaseValidationException(List<String> messages) {
        super();
        this.messages = messages;
    }

    public List<String> getMessages () {
        return messages;
    }
}
