package com.sk.rk.services.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SuccessResponse<T> extends BaseResponse {
    @JsonProperty("data")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private T data;

    public SuccessResponse() {
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
