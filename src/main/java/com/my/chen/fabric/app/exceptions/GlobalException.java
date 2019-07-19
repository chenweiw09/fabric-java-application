package com.my.chen.fabric.app.exceptions;

import lombok.Data;


@Data
public class GlobalException extends Exception{

    private int code;

    private String msg;

    public GlobalException(int code, String message) {
        super(message);
        this.code = code;
        this.msg = message;
    }

    public GlobalException(int code, Throwable cause) {
        super(cause);
        this.code = code;
        this.msg = cause.getMessage();
    }

}
