package com.szs.sungsu.exception;

import org.springframework.http.HttpStatus;

public abstract class CommonException extends RuntimeException {
    public CommonException(String message) {
        super(message);
    }

    public abstract HttpStatus getHttpStatus();
    public abstract String getMessage();
}
