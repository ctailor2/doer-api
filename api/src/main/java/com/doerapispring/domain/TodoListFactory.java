package com.doerapispring.domain;

import org.springframework.stereotype.Component;

@Component
public class TodoListFactory {
    TodoList todoList(UserId userId, ListId listId, String name) {
        return new TodoList(userId, listId, name);
    }
}
