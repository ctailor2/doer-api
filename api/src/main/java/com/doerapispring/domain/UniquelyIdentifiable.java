package com.doerapispring.domain;

public interface UniquelyIdentifiable<T> {
    UniqueIdentifier<T> getIdentifier();
}
