package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedUser;

public interface TodoApiService {
    TodoListDTO get(AuthenticatedUser authenticatedUser) throws InvalidRequestException;

    void create(AuthenticatedUser authenticatedUser, String task, String scheduling) throws InvalidRequestException;

    void delete(AuthenticatedUser authenticatedUser, Integer localId) throws InvalidRequestException;
}
