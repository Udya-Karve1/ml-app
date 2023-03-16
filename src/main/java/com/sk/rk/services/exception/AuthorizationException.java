package com.sk.rk.services.exception;

        import java.nio.file.AccessDeniedException;

public class AuthorizationException extends AccessDeniedException {

    public AuthorizationException(String message) {
        super(message);
    }

}
