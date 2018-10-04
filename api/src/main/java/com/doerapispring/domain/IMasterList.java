package com.doerapispring.domain;

import java.util.List;

public interface IMasterList {
    void add(TodoId todoId, String task) throws ListSizeExceededException, DuplicateTodoException;

    List<Todo> getTodos();

    void addDeferred(TodoId todoId, String task) throws DuplicateTodoException;

    void unlock() throws LockTimerNotExpiredException;

    List<Todo> getDeferredTodos();

    void delete(TodoId todoId) throws TodoNotFoundException;

    void displace(TodoId todoId, String task) throws TodoNotFoundException, DuplicateTodoException, ListNotFullException;

    void update(TodoId todoId, String task) throws TodoNotFoundException, DuplicateTodoException;

    String complete(TodoId todoId) throws TodoNotFoundException;

    void move(TodoId todoId, TodoId targetTodoId) throws TodoNotFoundException;

    boolean isAbleToBeUnlocked();

    boolean isLocked();

    Long unlockDuration();

    void pull();

    boolean isFull();

    boolean isAbleToBeReplenished();

    Integer getDemarcationIndex();
}
