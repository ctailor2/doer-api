package com.doerapispring.domain;

import java.time.Clock;
import java.util.Date;
import java.util.List;

public class CompletedList implements UniquelyIdentifiable<String> {
    private final Clock clock;
    private final UniqueIdentifier<String> uniqueIdentifier;
    private final List<CompletedTodo> todos;

    public CompletedList(Clock clock, UniqueIdentifier<String> uniqueIdentifier, List<CompletedTodo> todos) {
        this.clock = clock;
        this.uniqueIdentifier = uniqueIdentifier;
        this.todos = todos;
    }

    public void add(CompletedTodoId completedTodoId, String task) {
        todos.add(new CompletedTodo(completedTodoId, task, Date.from(clock.instant())));
    }

    public List<CompletedTodo> getAllTodos() {
        return todos;
    }

    @Override
    public UniqueIdentifier<String> getIdentifier() {
        return uniqueIdentifier;
    }

    ReadOnlyCompletedList read() {
        return new ReadOnlyCompletedList(todos);
    }
}
