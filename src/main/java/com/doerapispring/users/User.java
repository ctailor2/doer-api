package com.doerapispring.users;

import com.doerapispring.apiTokens.SessionToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chiragtailor on 8/11/16.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class User {
    private String email;
    private String password;
    private String passwordConfirmation;
    // Maybe that chunk ^ is a different domain object
    private SessionToken sessionToken;
}
