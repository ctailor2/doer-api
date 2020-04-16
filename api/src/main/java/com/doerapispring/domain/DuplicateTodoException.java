package com.doerapispring.domain;

class DuplicateTodoException extends DomainException {
    DuplicateTodoException() {
        super("Todo with task already exists");
    }
}
