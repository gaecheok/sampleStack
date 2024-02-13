package com.szs.sungsu.exception;

import org.springframework.http.HttpStatus;

public class MemberException extends CommonException {

    private final String message;

    public MemberException(String message) {
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
