package com.doerapispring.domain;

import com.doerapispring.web.InvalidRequestException;

import java.util.List;

public interface ListApplicationService {
    void unlock(User user, ListId listId) throws InvalidRequestException;

    ReadOnlyTodoList getDefault(User user) throws InvalidRequestException;

    List<CompletedTodo> getCompleted(User user, ListId listId) throws InvalidRequestException;

    ReadOnlyTodoList get(User user, ListId listId) throws InvalidRequestException;
}
