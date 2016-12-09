package com.doerapispring.apiTokens;

import com.doerapispring.UserIdentifier;
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
public class SessionToken {
    private String token;
    private Date expiresAt;

    @JsonIgnore
    private UserIdentifier userIdentifier;
}
