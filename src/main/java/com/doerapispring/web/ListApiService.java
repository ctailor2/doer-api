package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedUser;

import java.util.List;

public interface ListApiService {
    void unlock(AuthenticatedUser authenticatedUser) throws InvalidRequestException;

    List<ListDTO> getAll(AuthenticatedUser authenticatedUser) throws InvalidRequestException;
}
