package com.doerapispring.domain;

import java.util.List;

public class CompletedTodoList {
    private final UserId userId;
    private final ListId listId;
    private final List<CompletedTodo> completedTodos;

    public CompletedTodoList(
        UserId userId,
        ListId listId,
        List<CompletedTodo> completedTodos) {
        this.userId = userId;
        this.listId = listId;
        this.completedTodos = completedTodos;
    }

    public List<CompletedTodo> getCompletedTodos() {
        return completedTodos;
    }

    public ListId getListId() {
        return listId;
    }

    public UserId getUserId() {
        return userId;
    }
}
