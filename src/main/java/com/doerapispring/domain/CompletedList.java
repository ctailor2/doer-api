package com.doerapispring.domain;

import java.util.List;

public class CompletedList implements UniquelyIdentifiable<String> {
    private final UniqueIdentifier<String> uniqueIdentifier;
    private final List<CompletedTodo> todos;

    public CompletedList(UniqueIdentifier uniqueIdentifier, List<CompletedTodo> todos) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.todos = todos;
    }

    public List<CompletedTodo> getTodos() {
        return todos;
    }

    @Override
    public UniqueIdentifier<String> getIdentifier() {
        return uniqueIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompletedList that = (CompletedList) o;

        if (uniqueIdentifier != null ? !uniqueIdentifier.equals(that.uniqueIdentifier) : that.uniqueIdentifier != null)
            return false;
        return todos != null ? todos.equals(that.todos) : that.todos == null;

    }

    @Override
    public int hashCode() {
        int result = uniqueIdentifier != null ? uniqueIdentifier.hashCode() : 0;
        result = 31 * result + (todos != null ? todos.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CompletedList{" +
                "uniqueIdentifier=" + uniqueIdentifier +
                ", todos=" + todos +
                '}';
    }
}
