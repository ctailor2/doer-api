package com.doerapispring.domain;

public class DomainException extends IllegalStateException {
    public DomainException(String message) {
        super(message);
    }

    public DomainException() {}
}
