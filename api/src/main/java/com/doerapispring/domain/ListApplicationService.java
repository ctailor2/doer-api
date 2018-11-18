package com.doerapispring.domain;

import com.doerapispring.web.InvalidRequestException;

import java.util.List;

public interface ListApplicationService {
    void unlock(User user) throws InvalidRequestException;

    ReadOnlyTodoList get(User user) throws InvalidRequestException;

    List<CompletedTodo> getCompleted(User user) throws InvalidRequestException;
}
