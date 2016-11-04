package com.doerapispring.apiTokens;

import com.doerapispring.Identifier;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by chiragtailor on 8/28/16.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SessionToken {
    private String token;
    private Date expiresAt;

    @JsonIgnore
    private Identifier identifier;
}
