package com.sk.rk.services.exception;


import com.sk.rk.services.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.AccessDeniedException;
import java.util.*;
/**
 * @author uday.karve
 *
 * This class handles exception triggered while request
 * processing. An ErrorResponse object is created from
 * exception and send back to caller.
 */
@ControllerAdvice
@RestController
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {


    private static final String EXCEPTION = "Exception: ";

    @ExceptionHandler(value = {ConstraintViolationException.class})
    protected ResponseEntity<ErrorResponse> handleFieldValidationExceptions (ConstraintViolationException ex, HttpServletRequest request, HttpServletResponse response) {
        logger.error(EXCEPTION, ex);

        List<String> errors = new ArrayList<>();
        if (ex.getConstraintViolations() != null) {
            for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
                errors.add(violation.getPropertyPath().toString() + ": " + violation.getMessage());
            }
        }

        if (errors.isEmpty()){
            errors.add(ex.getMessage());
        }

        ErrorResponse error = prepareErrorResponse(ex, HttpStatus.BAD_REQUEST.value(), errors, request);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(value = {AccessDeniedException.class })
    protected ResponseEntity<ErrorResponse> handleAuthorizationException(AccessDeniedException ex, HttpServletRequest request, HttpServletResponse response) {
        logger.error(EXCEPTION, ex);

        List<String> errors = new ArrayList<>();

        if(!ex.getReason().isEmpty()) {
            errors.add(ex.getFile() + " " + ex.getMessage());
        }

        ErrorResponse error = prepareErrorResponse(ex, HttpStatus.UNAUTHORIZED.value(), errors, request);

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {BaseException.class })
    protected ResponseEntity<ErrorResponse> handleCommonExceptions (BaseException exception, HttpServletRequest request) {
        logger.error(EXCEPTION, exception);

        List<String> errors = new ArrayList<>();

        if (errors.isEmpty()){
            errors.add(exception.getMessage());
        }

        switch (exception.getErrorCode()) {
            case 400:
                return new ResponseEntity<>(prepareErrorResponse(exception, 400, errors, request), HttpStatus.BAD_REQUEST);
            case 404:
                return new ResponseEntity<>(prepareErrorResponse(exception, 404, errors, request), HttpStatus.NOT_FOUND);
            case 403:
                return new ResponseEntity<>(prepareErrorResponse(exception, 403, errors, request), HttpStatus.FORBIDDEN);
            case 401:
                return new ResponseEntity<>(prepareErrorResponse(exception, 401, errors, request), HttpStatus.UNAUTHORIZED);
            default:
                return new ResponseEntity<>(prepareErrorResponse(exception, 500, errors, request), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ExceptionHandler(value = {BaseRunTimeException.class })
    protected ResponseEntity<ErrorResponse> handleCommonExceptions (BaseRunTimeException exception, HttpServletRequest request) {
        logger.error(EXCEPTION, exception);

        List<String> errors = new ArrayList<>();

        if (errors.isEmpty()){
            errors.add(exception.getMessage());
        }

        switch (exception.getErrorCode()) {
            case 400:
                return new ResponseEntity<>(prepareErrorResponse(exception, 400, errors, request), HttpStatus.BAD_REQUEST);
            case 404:
                return new ResponseEntity<>(prepareErrorResponse(exception, 404, errors, request), HttpStatus.NOT_FOUND);
            case 403:
                return new ResponseEntity<>(prepareErrorResponse(exception, 403, errors, request), HttpStatus.FORBIDDEN);
            case 401:
                return new ResponseEntity<>(prepareErrorResponse(exception, 401, errors, request), HttpStatus.UNAUTHORIZED);
            default:
                return new ResponseEntity<>(prepareErrorResponse(exception, 500, errors, request), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @ExceptionHandler(value = {BaseValidationException.class })
    protected ResponseEntity<ErrorResponse> handleCommonExceptions (BaseValidationException exception, HttpServletRequest request) {
        logger.error(EXCEPTION, exception);

        List<String> errors = exception.getMessages();

        if (errors.isEmpty()){
            errors.add(exception.getMessage());
        }

        ErrorResponse error = prepareErrorResponse(exception, HttpStatus.BAD_REQUEST.value(), errors, request);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {Exception.class })
    protected ResponseEntity<ErrorResponse> handleCommonExceptions (Exception exception, HttpServletRequest request) {
        logger.error(EXCEPTION, exception);

        List<String> errors = new ArrayList<>();

        if (errors.isEmpty()){
            errors.add(exception.getMessage());
        }

        ErrorResponse error = prepareErrorResponse(exception, HttpStatus.BAD_REQUEST.value(), errors, request);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    protected ErrorResponse prepareErrorResponse(Exception exception, int code, List<String> userMessages, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setCode(code);
        error.setIsSuccess(false);
        error.setUserMessage(userMessages);
        error.setException(exception.getClass().getName());
        error.setStackTrace(getStackTrace(exception));
        error.setRequestAt(Objects.isNull(request.getAttribute("startTime")) ? new Date() : new Date((Long) request.getAttribute("startTime")));
        error.setPath(request.getRequestURI());

        return error;
    }

    protected String getStackTrace(Exception ex) {
        try {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            ex.printStackTrace(printWriter);
            return stringWriter.toString();
        } catch (Exception e) {
            return "";
        }
    }

    protected List<String> populateFieldErrorMessages(MethodArgumentNotValidException manve){

        if (manve != null){

            List<String> messages = new ArrayList<>();
            manve.getBindingResult().getFieldErrors().forEach(objectError ->
                    messages.add(objectError.getDefaultMessage())
            );
            return messages;
        }

        return Collections.emptyList();
    }
}
