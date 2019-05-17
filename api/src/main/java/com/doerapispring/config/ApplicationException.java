package com.doerapispring.config;

public class ApplicationException extends RuntimeException {
    protected ApplicationException(String message) {
        super(message);
    }

    ApplicationException() {
    }
}
