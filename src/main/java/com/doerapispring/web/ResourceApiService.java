package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedUser;

public interface ResourceApiService {
    TodoResourcesDTO getTodoResources(AuthenticatedUser authenticatedUser) throws InvalidRequestException;
}
