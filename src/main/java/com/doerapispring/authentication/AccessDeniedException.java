package com.doerapispring.authentication;

import com.doerapispring.config.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AccessDeniedException extends ApiException {
    public AccessDeniedException(String message) {
        super("Access denied - " + message);
    }
}
