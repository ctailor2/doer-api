package com.doerapispring.domain;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface OwnedObjectRepository<T, OwnerId, Id> {
    void save(T model) throws AbnormalModelException;

    default Optional<T> find(OwnerId ownerId, Id id) {
        return findOne(ownerId);
    }

    default Optional<T> findOne(OwnerId ownerId) {
        return Optional.empty();
    }

    default List<T> findAll(OwnerId ownerId) {
        return Collections.emptyList();
    }
}
