package com.doerapispring.domain;

import java.sql.Date;
import java.time.Clock;
import java.util.List;
import java.util.UUID;

public class CompletedList implements UniquelyIdentifiable<String> {
    private final Clock clock;
    private final UniqueIdentifier<String> uniqueIdentifier;
    private final List<CompletedTodo> todos;

    public CompletedList(Clock clock, UniqueIdentifier<String> uniqueIdentifier, List<CompletedTodo> todos) {
        this.clock = clock;
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

    public void add(String task) {
        todos.add(new CompletedTodo(UUID.randomUUID().toString(), task, Date.from(clock.instant())));
    }
}
