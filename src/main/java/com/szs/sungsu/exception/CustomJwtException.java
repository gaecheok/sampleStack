package com.szs.sungsu.exception;

import com.szs.sungsu.exception.CommonException;
import org.springframework.http.HttpStatus;

public class CustomJwtException extends CommonException {

    private final String message;

    public CustomJwtException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
