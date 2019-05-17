package com.doerapispring.authentication;

import com.doerapispring.config.ApplicationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AccessDeniedException extends ApplicationException {
    AccessDeniedException(String message) {
        super("Access denied - " + message);
    }
}
