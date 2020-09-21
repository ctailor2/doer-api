package com.doerapispring.domain;

import com.doerapispring.domain.DomainException;

class DuplicateTodoException extends DomainException {
    DuplicateTodoException() {
        super("Todo with task already exists");
    }
}
