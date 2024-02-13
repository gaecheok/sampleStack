package com.szs.sungsu.exception;

import com.szs.sungsu.exception.CommonException;
import org.springframework.http.HttpStatus;

public class RemoteAPIException extends CommonException {
    private final String message;

    public RemoteAPIException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Override
    public String getMessage() {
        return message;
    }


}
