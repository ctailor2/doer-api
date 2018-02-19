package com.doerapispring.web;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class SessionTokenDTO {
    @JsonProperty("token")
    private final String token;

    @JsonProperty("expiresAt")
    private final Date expiresAt;

    public SessionTokenDTO(String token,
                           Date expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SessionTokenDTO that = (SessionTokenDTO) o;

        if (token != null ? !token.equals(that.token) : that.token != null) return false;
        return expiresAt != null ? expiresAt.equals(that.expiresAt) : that.expiresAt == null;

    }

    @Override
    public int hashCode() {
        int result = token != null ? token.hashCode() : 0;
        result = 31 * result + (expiresAt != null ? expiresAt.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SessionTokenDTO{" +
                "token='" + token + '\'' +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
