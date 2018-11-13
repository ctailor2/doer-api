package com.doerapispring.domain;

import java.util.Optional;

public interface OwnedObjectRepository<T, OwnerId, Id> {
    default void add(T model) {}

    default void save(T model) throws AbnormalModelException {}

    default Optional<T> find(OwnerId ownerId, Id id) {
        return findOne(ownerId);
    }

    Optional<T> findOne(OwnerId ownerId);

//    TODO: Remove this once it is no longer being used to generate TodoId's
    UniqueIdentifier<String> nextIdentifier();
}
