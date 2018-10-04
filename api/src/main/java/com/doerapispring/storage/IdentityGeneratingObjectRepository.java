package com.doerapispring.storage;

import com.doerapispring.domain.ObjectRepository;
import com.doerapispring.domain.UniqueIdentifier;
import com.doerapispring.domain.UniquelyIdentifiable;

public interface IdentityGeneratingObjectRepository<T extends UniquelyIdentifiable, U> extends ObjectRepository<T, U> {
    UniqueIdentifier<U> nextIdentifier();
}
