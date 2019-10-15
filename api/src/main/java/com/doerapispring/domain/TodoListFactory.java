package com.doerapispring.domain;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Component
public class TodoListFactory {
    TodoList todoList(UserId userId, ListId listId, String name) {
        return new TodoList(
            userId,
            listId,
            name,
            0,
            Date.from(Instant.EPOCH));
    }
}
