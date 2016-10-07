package com.doerapispring.apiTokens;

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
public class SessionTokenEntity {
    private String token;
    private Date expiresAt;
}
