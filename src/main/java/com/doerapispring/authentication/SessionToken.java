package com.doerapispring.authentication;

import com.doerapispring.domain.UniquelyIdentifiable;
import com.doerapispring.domain.UniqueIdentifier;
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
public class SessionToken implements UniquelyIdentifiable<String> {
    private UniqueIdentifier<String> uniqueIdentifier;

    private String token;
    private Date expiresAt;

    @JsonIgnore
    private UniqueIdentifier<String> userIdentifier;

    @Override
    @JsonIgnore
    // TODO: Fix this - should not have two fields with type UniqueIdentifier
    public UniqueIdentifier<String> getIdentifier() {
        return uniqueIdentifier;
    }
}
