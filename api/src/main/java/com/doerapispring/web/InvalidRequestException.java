package com.doerapispring.web;

import com.doerapispring.config.ApiException;

public class InvalidRequestException extends ApiException {
    public InvalidRequestException(String message) {
        super(message);
    }

    // TODO: Remove this constructor - eventually nobody should call it
    public InvalidRequestException() {
        super();
    }
}
