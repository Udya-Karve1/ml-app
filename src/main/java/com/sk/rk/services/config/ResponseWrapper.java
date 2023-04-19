package com.sk.rk.services.config;

import com.sk.rk.services.model.BaseResponse;
import com.sk.rk.services.model.ErrorResponse;
import com.sk.rk.services.model.SuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;


import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

// @RestControllerAdvice
@Slf4j
public class ResponseWrapper /*implements ResponseBodyAdvice*/ {

    //@Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    // @Override
    public BaseResponse beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType
            , Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {


        BaseResponse respond = null;
        HttpServletResponse servletResponse = ((ServletServerHttpResponse)response).getServletResponse();
        HttpServletRequest servletRequest = ((ServletServerHttpRequest)request).getServletRequest();
        int statusCode = servletResponse.getStatus();

        if (body instanceof ErrorResponse) {
            return (ErrorResponse)body;
        } else {
            if (statusCode >= 200 && statusCode < 300) {
                SuccessResponse successResponse = new SuccessResponse();
                successResponse.setIsSuccess(true);
                successResponse.setData(body);
                respond = successResponse;
            } else {
                Map<String, Object> responseMap = (Map)body;
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setIsSuccess(false);
                errorResponse.setPath(responseMap.get("path").toString());
                errorResponse.setException(responseMap.get("message").toString());
                if (responseMap.get("message") != null && responseMap.get("message").toString().equalsIgnoreCase("Token expire")) {
                    errorResponse.setUserMessage(Collections.singletonList(responseMap.get("error").toString()));
                    errorResponse.setCode(401);
                } else {
                    errorResponse.setUserMessage(Collections.singletonList(responseMap.get("error").toString()));
                    errorResponse.setCode(statusCode);
                }

                errorResponse.setStackTrace("");
                respond = errorResponse;
            }

            ((BaseResponse)respond).setRequestAt(Objects.isNull(servletRequest.getAttribute("startTime")) ? new Date() : new Date((Long)servletRequest.getAttribute("startTime")));
        }
        return (BaseResponse)respond;

    }


}
