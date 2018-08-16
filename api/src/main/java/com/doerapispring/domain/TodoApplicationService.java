package com.doerapispring.domain;

import com.doerapispring.web.InvalidRequestException;

public interface TodoApplicationService {
    void create(User user, String task) throws InvalidRequestException;

    void createDeferred(User user, String task) throws InvalidRequestException;

    void delete(User user, String localId) throws InvalidRequestException;

    void displace(User user, String task) throws InvalidRequestException;

    void update(User user, String localId, String task) throws InvalidRequestException;

    void complete(User user, String localId) throws InvalidRequestException;

    void move(User user, String localId, String targetLocalId) throws InvalidRequestException;

    void pull(User user) throws InvalidRequestException;
}
