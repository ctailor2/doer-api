package com.doerapispring.domain;

import java.util.List;

public interface IMasterList {
    void add(TodoId todoId, String task) throws ListSizeExceededException, DuplicateTodoException;

    List<Todo> getTodos();

    void addDeferred(TodoId todoId, String task) throws DuplicateTodoException;

    void unlock() throws LockTimerNotExpiredException;

    List<Todo> getDeferredTodos();

    void delete(String localIdentifier) throws TodoNotFoundException;

    void displace(TodoId todoId, String task) throws TodoNotFoundException, DuplicateTodoException, ListNotFullException;

    void update(String localIdentifier, String task) throws TodoNotFoundException, DuplicateTodoException;

    String complete(String localIdentifier) throws TodoNotFoundException;

    void move(String localIdentifier, String targetLocalIdentifier) throws TodoNotFoundException;

    boolean isAbleToBeUnlocked();

    boolean isLocked();

    Long unlockDuration();

    void pull();

    boolean isFull();

    boolean isAbleToBeReplenished();

    String getTask(String localIdentifier);

    Integer getDemarcationIndex();
}
