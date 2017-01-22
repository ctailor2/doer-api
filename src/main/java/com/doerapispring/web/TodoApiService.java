package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedUser;

public interface TodoApiService {
    TodoList get(AuthenticatedUser authenticatedUser) throws InvalidRequestException;

    void create(AuthenticatedUser authenticatedUser, String task, String scheduling) throws InvalidRequestException;
}
