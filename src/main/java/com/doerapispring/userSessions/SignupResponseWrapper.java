package com.doerapispring.userSessions;

import com.doerapispring.apiTokens.SessionTokenEntity;
import com.doerapispring.users.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chiragtailor on 8/12/16.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SignupResponseWrapper {
    private UserEntity user;
    private SessionTokenEntity sessionToken;
}
