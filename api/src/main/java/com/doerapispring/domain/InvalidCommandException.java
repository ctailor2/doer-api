package com.doerapispring.domain;

class InvalidCommandException extends DomainException {
    InvalidCommandException(String message) {
        super(message);
    }

    // TODO: Remove this constructor - eventually nobody should call it
    InvalidCommandException() {
        super();
    }
}
