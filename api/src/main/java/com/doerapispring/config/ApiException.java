package com.doerapispring.config;

public class ApiException extends RuntimeException {
    protected ApiException(String message) {
        super(message);
    }

    public ApiException() {
    }
}
