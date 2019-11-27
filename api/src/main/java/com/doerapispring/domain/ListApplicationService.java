package com.doerapispring.domain;

import java.util.List;

public interface ListApplicationService {
    void unlock(User user, ListId listId) throws InvalidCommandException;

    TodoListReadModel getDefault(User user) throws InvalidCommandException;

    CompletedTodoList getCompleted(User user, ListId listId) throws InvalidCommandException;

    TodoListReadModel get(User user, ListId listId) throws InvalidCommandException;

    List<TodoList> getAll(User user);

    void create(User user, String name);
}
