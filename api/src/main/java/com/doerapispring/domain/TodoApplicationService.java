package com.doerapispring.domain;

public interface TodoApplicationService {
    void create(User user, ListId listId, String task);

    void createDeferred(User user, ListId listId, String task);

    void delete(User user, ListId listId, TodoId todoId);

    void displace(User user, ListId listId, String task);

    void update(User user, ListId listId, TodoId todoId, String task);

    void complete(User user, ListId listId, TodoId todoId);

    void move(User user, ListId listId, TodoId todoId, TodoId targetTodoId);

    void pull(User user, ListId listId);

    void escalate(User user, ListId listId);
}
