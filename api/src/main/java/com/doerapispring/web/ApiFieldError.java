package com.doerapispring.web;

public class ApiFieldError {
    private final String field;
    private final String message;

    public ApiFieldError(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }
}
