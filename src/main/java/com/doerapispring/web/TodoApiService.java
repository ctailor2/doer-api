package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedUser;

public interface TodoApiService {
    TodoListDTO get(AuthenticatedUser authenticatedUser) throws InvalidRequestException;

    void create(AuthenticatedUser authenticatedUser, String task, String scheduling) throws InvalidRequestException;

    void delete(AuthenticatedUser authenticatedUser, String localId) throws InvalidRequestException;

    void displace(AuthenticatedUser authenticatedUser, String localId, String task) throws InvalidRequestException;

    void update(AuthenticatedUser authenticatedUser, String localId, String task) throws InvalidRequestException;

    void complete(AuthenticatedUser authenticatedUser, String localId) throws InvalidRequestException;

    CompletedTodoListDTO getCompleted(AuthenticatedUser authenticatedUser) throws InvalidRequestException;
}
