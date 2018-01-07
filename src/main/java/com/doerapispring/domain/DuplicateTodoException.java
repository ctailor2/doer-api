package com.doerapispring.domain;

class DuplicateTodoException extends Exception {
    DuplicateTodoException(String message) {
        super(message);
    }

    DuplicateTodoException() {
        super("Todo with task already exists");
    }
}
