package com.doerapispring;

public class UserIdentifier {
    private final String identifier;

    public UserIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String get() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserIdentifier that = (UserIdentifier) o;

        return identifier != null ? identifier.equals(that.identifier) : that.identifier == null;

    }

    @Override
    public int hashCode() {
        return identifier != null ? identifier.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "UserIdentifier{" +
                "identifier='" + identifier + '\'' +
                '}';
    }
}
