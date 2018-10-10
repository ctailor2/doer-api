package com.doerapispring.domain;

import java.util.List;

public class ReadOnlyCompletedList {
    private final List<CompletedTodo> todos;

    ReadOnlyCompletedList(List<CompletedTodo> todos) {
        this.todos = todos;
    }

    public List<CompletedTodo> getTodos() {
        return todos;
    }
}
