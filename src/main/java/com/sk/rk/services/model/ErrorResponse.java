package com.sk.rk.services.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sk.rk.services.utils.Constants;
import lombok.Data;

import java.util.List;

@Data
public class ErrorResponse extends BaseResponse {

    @JsonProperty(Constants.RESPONSE_CODE)
    private int code;

    @JsonProperty(Constants.RESPONSE_USER_MESSAGE_FIELD)
    private List<String> userMessage;

    @JsonProperty(Constants.RESPONSE_PATH_FIELD)
    private String path;

    @JsonProperty(Constants.RESPONSE_EXCEPTION_FIELD)
    private String exception;

    @JsonIgnore
    private String stackTrace;

    public ErrorResponse() {
    }

    public ErrorResponse(int code, List<String> userMessage,
                 String systemMessage,
                 String exception,
                 String stackTrace,
                 String path,
                 String detailLink) {
        setCode(code);
        setUserMessage(userMessage);
        setException(exception);
        setStackTrace(stackTrace);
        setPath(path);
    }
}
