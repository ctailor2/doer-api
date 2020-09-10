package com.doerapispring.domain;

public class CompletedTodoId {
    private final String identifier;

    public CompletedTodoId(String identifier) {
        this.identifier = identifier;
    }

    public String get() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompletedTodoId that = (CompletedTodoId) o;

        return identifier != null ? identifier.equals(that.identifier) : that.identifier == null;
    }

    @Override
    public int hashCode() {
        return identifier != null ? identifier.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "CompletedTodoId{" +
            "identifier='" + identifier + '\'' +
            '}';
    }
}
