package com.doerapispring.web;

public class GlobalError {
    private final String message;

    public GlobalError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
