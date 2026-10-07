package com.university.shared.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {
    private final HttpStatus status;

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }

    public static BusinessException missing(String what) {
        return new BusinessException(HttpStatus.NOT_FOUND, what + " not found");
    }

    public static BusinessException conflict(String why) {
        return new BusinessException(HttpStatus.CONFLICT, why);
    }
}
