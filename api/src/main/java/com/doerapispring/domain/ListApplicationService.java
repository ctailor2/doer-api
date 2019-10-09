package com.doerapispring.domain;

import java.util.List;

public interface ListApplicationService {
    void unlock(User user, ListId listId) throws InvalidCommandException;

    ReadOnlyTodoList getDefault(User user) throws InvalidCommandException;

    List<CompletedTodo> getCompleted(User user, ListId listId) throws InvalidCommandException;

    ReadOnlyTodoList get(User user, ListId listId) throws InvalidCommandException;

    List<ListOverview> getOverviews(User user);
}
