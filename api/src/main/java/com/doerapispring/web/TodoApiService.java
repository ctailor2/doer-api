package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedUser;

public interface TodoApiService {
    void create(AuthenticatedUser authenticatedUser, String task) throws InvalidRequestException;

    void createDeferred(AuthenticatedUser authenticatedUser, String task) throws InvalidRequestException;

    void delete(AuthenticatedUser authenticatedUser, String localId) throws InvalidRequestException;

    void displace(AuthenticatedUser authenticatedUser, String localId, String task) throws InvalidRequestException;

    void update(AuthenticatedUser authenticatedUser, String localId, String task) throws InvalidRequestException;

    void complete(AuthenticatedUser authenticatedUser, String localId) throws InvalidRequestException;

    CompletedTodoListDTO getCompleted(AuthenticatedUser authenticatedUser) throws InvalidRequestException;

    void move(AuthenticatedUser authenticatedUser, String localId, String targetLocalId) throws InvalidRequestException;

    void pull(AuthenticatedUser authenticatedUser) throws InvalidRequestException;

    TodoListDTO getTodos(AuthenticatedUser authenticatedUser) throws InvalidRequestException;

    TodoListDTO getDeferredTodos(AuthenticatedUser authenticatedUser) throws InvalidRequestException;
}