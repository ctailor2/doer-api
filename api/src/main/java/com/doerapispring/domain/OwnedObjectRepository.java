package com.doerapispring.domain;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface OwnedObjectRepository<T, OwnerId, Id> extends IdentityGeneratingRepository<Id> {
    void save(T model) throws AbnormalModelException;

    default Optional<T> find(OwnerId ownerId, Id id) {
        return findFirst(ownerId);
    }

    default Optional<T> findFirst(OwnerId ownerId) {
        return findAll(ownerId).stream().findFirst();
    }

    default List<T> findAll(OwnerId ownerId) {
        return Collections.emptyList();
    }
}
