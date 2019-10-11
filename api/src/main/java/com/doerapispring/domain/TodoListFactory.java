package com.doerapispring.domain;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

@Component
public class TodoListFactory {
    private final Clock clock;

    public TodoListFactory(Clock clock) {
        this.clock = clock;
    }

    TodoList todoList(UserId userId, ListId listId, String name) {
        return new TodoList(
            clock,
            userId,
            listId,
            name,
            Date.from(Instant.EPOCH),
            new ArrayList<>(),
            0);
    }

    ListOverview listOverview(UserId userId, ListId listId, String name) {
        return new ListOverview(
            userId,
            listId,
            name,
            0,
            Date.from(Instant.EPOCH));
    }
}
