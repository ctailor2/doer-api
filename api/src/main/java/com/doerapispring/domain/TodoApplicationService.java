package com.doerapispring.domain;

public interface TodoApplicationService {
    void create(User user, ListId listId, String task) throws InvalidCommandException;

    void createDeferred(User user, ListId listId, String task) throws InvalidCommandException;

    void delete(User user, ListId listId, TodoId todoId) throws InvalidCommandException;

    void displace(User user, ListId listId, String task) throws InvalidCommandException;

    void update(User user, ListId listId, TodoId todoId, String task) throws InvalidCommandException;

    void complete(User user, ListId listId, TodoId todoId) throws InvalidCommandException;

    void move(User user, ListId listId, TodoId todoId, TodoId targetTodoId) throws InvalidCommandException;

    void pull(User user, ListId listId) throws InvalidCommandException;

    void escalate(User user, ListId listId) throws InvalidCommandException;
}
