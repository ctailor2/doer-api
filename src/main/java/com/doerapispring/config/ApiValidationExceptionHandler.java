package com.doerapispring.config;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.toList;

@RestControllerAdvice
public class ApiValidationExceptionHandler extends ResponseEntityExceptionHandler {
    private static final String DEFAULT_FIELD_ERROR_MESSAGE = "value was rejected";
    private static final String DEFAULT_GLOBAL_ERROR_MESSAGE = "an error occurred";
    private final MessageSource messageSource;

    ApiValidationExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException exception,
        HttpHeaders headers,
        HttpStatus status,
        WebRequest request
    ) {
        BindingResult bindingResult = exception.getBindingResult();

        List<ApiFieldError> fieldErrors = bindingResult.getFieldErrors().stream()
            .map(fieldError -> new ApiFieldError(
                fieldError.getField(),
                buildMessageOrUseDefault(fieldError, DEFAULT_FIELD_ERROR_MESSAGE)))
            .collect(toList());

        List<GlobalError> globalErrors = bindingResult.getGlobalErrors().stream()
            .map(globalError -> new GlobalError(buildMessageOrUseDefault(globalError, DEFAULT_GLOBAL_ERROR_MESSAGE)))
            .collect(toList());

        return new ResponseEntity<>(new ErrorResponse(fieldErrors, globalErrors), HttpStatus.BAD_REQUEST);
    }

    private String buildMessageOrUseDefault(ObjectError objectError, String defaultMessage) {
        return messageSource.getMessage(
            objectError.getCode(),
            objectError.getArguments(),
            defaultMessage,
            Locale.getDefault());
    }

    private class ErrorResponse {
        private final List<ApiFieldError> fieldErrors;
        private final List<GlobalError> globalErrors;

        ErrorResponse(List<ApiFieldError> fieldErrors, List<GlobalError> globalErrors) {
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

    private class ApiFieldError {
        private final String field;
        private final String message;

        ApiFieldError(String field, String message) {
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

    private class GlobalError {
        private final String message;

        GlobalError(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
