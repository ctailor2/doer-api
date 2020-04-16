package com.doerapispring.domain;

import java.util.List;

public interface ListApplicationService {
    void unlock(User user, ListId listId);

    TodoListReadModel getDefault(User user);

    CompletedTodoList getCompleted(User user, ListId listId);

    TodoListReadModel get(User user, ListId listId);

    List<TodoList> getAll(User user);

    void create(User user, String name);

    void setDefault(User user, ListId listId);
}
