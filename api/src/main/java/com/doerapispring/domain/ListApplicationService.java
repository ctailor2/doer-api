package com.doerapispring.domain;

import com.doerapispring.web.InvalidRequestException;

public interface ListApplicationService {
    void unlock(User user) throws InvalidRequestException;

    ReadOnlyTodoList get(User user) throws InvalidRequestException;

    ReadOnlyCompletedList getCompleted(User user) throws InvalidRequestException;
}