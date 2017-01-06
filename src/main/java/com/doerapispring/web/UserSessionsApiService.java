package com.doerapispring.web;

import com.doerapispring.authentication.AccessDeniedException;

public interface UserSessionsApiService {
    SessionTokenDTO signup(String userIdentifier, String userCredentials) throws AccessDeniedException;

    SessionTokenDTO login(String userIdentifier, String userCredentials) throws AccessDeniedException;
}