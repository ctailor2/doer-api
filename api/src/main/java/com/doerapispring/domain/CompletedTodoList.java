package com.doerapispring.domain;

import java.util.List;
import java.util.Objects;

public class CompletedTodoList {
    private final UserId userId;
    private final ListId listId;
    private List<CompletedTodoReadModel> todos;

    public CompletedTodoList(UserId userId,
                             ListId listId,
                             List<CompletedTodoReadModel> todos) {
        this.userId = userId;
        this.listId = listId;
        this.todos = todos;
    }

    public List<CompletedTodoReadModel> getTodos() {
        return todos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompletedTodoList that = (CompletedTodoList) o;
        return Objects.equals(userId, that.userId) &&
            Objects.equals(listId, that.listId) &&
            Objects.equals(todos, that.todos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, listId, todos);
    }

    @Override
    public String toString() {
        return "CompletedTodoList{" +
            "userId=" + userId +
            ", listId=" + listId +
            ", todos=" + todos +
            '}';
    }
}
