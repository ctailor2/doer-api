package com.doerapispring.config;

public class ApiException extends Exception {
    protected ApiException(String message) {
        super(message);
    }

    public ApiException() {
    }
}
