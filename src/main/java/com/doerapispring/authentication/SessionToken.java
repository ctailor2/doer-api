package com.doerapispring.authentication;

import com.doerapispring.domain.UniquelyIdentifiable;
import com.doerapispring.domain.UserIdentifier;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SessionToken implements UniquelyIdentifiable {
    private SessionTokenIdentifier sessionTokenIdentifier;

    private String token;
    private Date expiresAt;

    @JsonIgnore
    private UserIdentifier userIdentifier;

    @Override
    @JsonIgnore
    public SessionTokenIdentifier getIdentifier() {
        return sessionTokenIdentifier;
    }
}
