package com.doerapispring.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.ResourceSupport;

public class SessionResponse extends ResourceSupport {
    @JsonProperty("session")
    private final SessionTokenDTO sessionTokenDTO;

    SessionResponse(SessionTokenDTO sessionTokenDTO) {
        this.sessionTokenDTO = sessionTokenDTO;
    }

    public SessionTokenDTO getSessionTokenDTO() {
        return sessionTokenDTO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SessionResponse that = (SessionResponse) o;

        return sessionTokenDTO != null ? sessionTokenDTO.equals(that.sessionTokenDTO) : that.sessionTokenDTO == null;

    }

    @Override
    public int hashCode() {
        return sessionTokenDTO != null ? sessionTokenDTO.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "SessionResponse{" +
                "sessionTokenDTO=" + sessionTokenDTO +
                '}';
    }
}
