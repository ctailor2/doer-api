package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedUser;

import java.util.List;

public interface TodoApiService {
    List<TodoDTO> getByScheduling(AuthenticatedUser authenticatedUser, String scheduling) throws InvalidRequestException;

    TodoList get(AuthenticatedUser authenticatedUser) throws InvalidRequestException;

    void create(AuthenticatedUser authenticatedUser, String task, String scheduling) throws InvalidRequestException;
}
