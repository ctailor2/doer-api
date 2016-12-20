package com.doerapispring.domain;

import java.util.Optional;

public interface DomainRepository<T, U> {
    default void add(T model) throws AbnormalModelException {

    }

    default Optional<T> find(UniqueIdentifier<U> uniqueIdentifier) {
        return Optional.empty();
    }
}
