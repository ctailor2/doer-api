package com.doerapispring.domain;

class DuplicateTodoException extends Exception {
    DuplicateTodoException() {
        super("Todo with task already exists");
    }
}
