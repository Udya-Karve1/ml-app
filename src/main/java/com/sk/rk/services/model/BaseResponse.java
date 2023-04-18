package com.sk.rk.services.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sk.rk.services.utils.Constants;

import java.io.Serializable;
import java.util.Date;

public class BaseResponse implements Serializable {

    @JsonProperty(Constants.TIMESTAMP_FIELD)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYYY-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date requestAt;

    @JsonProperty(Constants.IS_SUCCESS)
    public boolean isSuccess;

    public Date getRequestAt() {
        return requestAt;
    }
    public void setRequestAt(Date requestAt) {
        this.requestAt = requestAt;
    }

    public boolean getIsSuccess () {
        return isSuccess;
    }
    public void setIsSuccess (boolean isSuccess) {
        this.isSuccess = isSuccess;
    }
}
