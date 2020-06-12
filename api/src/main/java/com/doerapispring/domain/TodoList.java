package com.doerapispring.domain;

import java.util.Objects;

public class TodoList {
    private final UserId userId;
    private final ListId listId;
    private final String name;

    public TodoList(UserId userId, ListId listId, String name) {
        this.userId = userId;
        this.listId = listId;
        this.name = name;
    }

    public UserId getUserId() {
        return userId;
    }

    public ListId getListId() {
        return listId;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "TodoList{" +
                "userId=" + userId +
                ", listId=" + listId +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TodoList todoList = (TodoList) o;
        return Objects.equals(userId, todoList.userId) &&
                Objects.equals(listId, todoList.listId) &&
                Objects.equals(name, todoList.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, listId, name);
    }
}
