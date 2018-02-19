package com.doerapispring.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public class UniqueIdentifier<T> {
    private final T identifier;

    @JsonCreator
    public UniqueIdentifier(T identifier) {
        this.identifier = identifier;
    }

    public T get() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UniqueIdentifier that = (UniqueIdentifier) o;

        return identifier != null ? identifier.equals(that.identifier) : that.identifier == null;

    }

    @Override
    public int hashCode() {
        return identifier != null ? identifier.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "UniqueIdentifier{" +
                "identifier=" + identifier +
                '}';
    }
}
