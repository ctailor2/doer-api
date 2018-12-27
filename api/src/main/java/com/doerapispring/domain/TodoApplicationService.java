package com.doerapispring.domain;

import com.doerapispring.web.InvalidRequestException;

public interface TodoApplicationService {
    void create(User user, ListId listId, String task) throws InvalidRequestException;

    void createDeferred(User user, ListId listId, String task) throws InvalidRequestException;

    void delete(User user, TodoId todoId) throws InvalidRequestException;

    void displace(User user, String task) throws InvalidRequestException;

    void update(User user, TodoId todoId, String task) throws InvalidRequestException;

    void complete(User user, ListId listId, TodoId todoId) throws InvalidRequestException;

    void move(User user, TodoId todoId, TodoId targetTodoId) throws InvalidRequestException;

    void pull(User user) throws InvalidRequestException;

    void escalate(User user) throws InvalidRequestException;
}
