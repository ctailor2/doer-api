package com.doerapispring.domain;

import java.util.List;

public class ImmediateList {
    private final List<Todo> todos;

    public ImmediateList(List<Todo> todos) {
        this.todos = todos;
    }

    public List<Todo> getTodos() {
        return todos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImmediateList that = (ImmediateList) o;

        return todos != null ? todos.equals(that.todos) : that.todos == null;

    }

    @Override
    public int hashCode() {
        return todos != null ? todos.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ImmediateList{" +
                "todos=" + todos +
                '}';
    }
}
