package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedUser;

public interface ListApiService {
    void unlock(AuthenticatedUser authenticatedUser) throws InvalidRequestException;
}
