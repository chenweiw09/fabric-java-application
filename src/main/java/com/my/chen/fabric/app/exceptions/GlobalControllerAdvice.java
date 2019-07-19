package com.my.chen.fabric.app.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@ControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public Object handleException(HttpServletRequest request, HttpServletResponse response, Throwable e) {
        response.setStatus(412);
        GlobalException exception = new GlobalException(412, e);
        log.warn("handleException|error handle:" + request.getRequestURI() + "|e:" + e.getMessage());
        return exception;
    }
}
