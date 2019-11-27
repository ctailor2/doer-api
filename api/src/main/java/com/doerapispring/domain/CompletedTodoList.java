package com.doerapispring.domain;

import java.util.List;

public class CompletedTodoList {
    private List<CompletedTodo> todos;

    public CompletedTodoList(List<CompletedTodo> todos) {
        this.todos = todos;
    }

    public List<CompletedTodo> getTodos() {
        return todos;
    }
}
