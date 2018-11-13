package com.doerapispring.domain;

public interface IdentityGeneratingObjectRepository<T extends UniquelyIdentifiable, U> extends ObjectRepository<T, U> {
    UniqueIdentifier<U> nextIdentifier();
}
