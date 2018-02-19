package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedUser;

public interface ListApiService {
    void unlock(AuthenticatedUser authenticatedUser) throws InvalidRequestException;

    MasterListDTO get(AuthenticatedUser authenticatedUser) throws InvalidRequestException;
}
