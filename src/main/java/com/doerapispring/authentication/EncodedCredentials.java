package com.doerapispring.authentication;

public class EncodedCredentials {
    private final String encodedCredentials;

    public EncodedCredentials(String encodedCredentials) {
        this.encodedCredentials = encodedCredentials;
    }

    public String get() {
        return encodedCredentials;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EncodedCredentials that = (EncodedCredentials) o;

        return encodedCredentials != null ? encodedCredentials.equals(that.encodedCredentials) : that.encodedCredentials == null;

    }

    @Override
    public int hashCode() {
        return encodedCredentials != null ? encodedCredentials.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "EncodedCredentials{" +
                "encodedCredentials='" + encodedCredentials + '\'' +
                '}';
    }
}
