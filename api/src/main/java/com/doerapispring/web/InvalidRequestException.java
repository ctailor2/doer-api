package com.doerapispring.web;

import com.doerapispring.config.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRequestException extends ApiException {
    public InvalidRequestException(String message) {
        super(message);
    }

    // TODO: Remove this constructor - eventually nobody should call it
    public InvalidRequestException() {
        super();
    }
}
