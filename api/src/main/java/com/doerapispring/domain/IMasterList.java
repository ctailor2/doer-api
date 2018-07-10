package com.doerapispring.domain;

import java.util.List;

public interface IMasterList {
    Todo add(String task) throws ListSizeExceededException, DuplicateTodoException;

    List<Todo> getTodos();

    Todo addDeferred(String task) throws DuplicateTodoException;

    ListUnlock unlock() throws LockTimerNotExpiredException;

    List<Todo> getDeferredTodos() throws LockTimerNotExpiredException;

    Todo delete(String localIdentifier) throws TodoNotFoundException;

    Todo displace(String task) throws TodoNotFoundException, DuplicateTodoException;

    Todo update(String localIdentifier, String task) throws TodoNotFoundException, DuplicateTodoException;

    String complete(String localIdentifier) throws TodoNotFoundException;

    List<Todo> move(String localIdentifier, String targetLocalIdentifier) throws TodoNotFoundException;

    boolean isAbleToBeUnlocked();

    boolean isLocked();

    Long unlockDuration();

    List<Todo> pull();

    boolean isFull();

    boolean isAbleToBeReplenished();

    String getTask(String localIdentifier);

    Integer getDemarcationIndex();
}
