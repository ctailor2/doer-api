package com.doerapispring.web;

import java.util.List;

public class ErrorResponse {
    private final List<ApiFieldError> fieldErrors;
    private final List<GlobalError> globalErrors;

    public ErrorResponse(List<ApiFieldError> fieldErrors, List<GlobalError> globalErrors) {
        this.fieldErrors = fieldErrors;
        this.globalErrors = globalErrors;
    }

    public List<ApiFieldError> getFieldErrors() {
        return fieldErrors;
    }

    public List<GlobalError> getGlobalErrors() {
        return globalErrors;
    }
}
