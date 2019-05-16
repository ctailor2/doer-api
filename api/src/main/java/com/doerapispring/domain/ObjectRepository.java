package com.doerapispring.domain;

import java.util.Optional;

public interface ObjectRepository<T, Id> {
    void save(T model);

    Optional<T> find(Id id);
}
