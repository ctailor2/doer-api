package com.doerapispring.domain;

import java.util.Optional;

public interface ObjectRepository<T extends UniquelyIdentifiable, U> {
    default void add(T model) {}

    default void save(T model) throws AbnormalModelException {}

    Optional<T> find(UniqueIdentifier<U> uniqueIdentifier);
}
