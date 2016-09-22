package com.doerapispring.userSessions;

import com.doerapispring.apiTokens.SessionToken;
import com.doerapispring.apiTokens.SessionTokenEntity;
import com.doerapispring.apiTokens.SessionTokenService;
import com.doerapispring.users.User;
import com.doerapispring.users.UserEntity;
import com.doerapispring.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by chiragtailor on 9/1/16.
 */
@Service
public class UserSessionsService {
    private final UserService userService;
    private final SessionTokenService sessionTokenService;
    private final AuthenticationService authenticationService;

    @Autowired
    public UserSessionsService(UserService userService, SessionTokenService sessionTokenService, AuthenticationService authenticationService) {
        this.userService = userService;
        this.sessionTokenService = sessionTokenService;
        this.authenticationService = authenticationService;
    }

    public UserEntity signup(UserEntity userEntity) {
        User savedUser = userService.create(userEntity);
        SessionToken savedSessionToken = sessionTokenService.create(savedUser.id);

        SessionTokenEntity sessionTokenEntity = SessionTokenEntity.builder()
                .token(savedSessionToken.token)
                .build();
        return UserEntity.builder()
                .email(savedUser.email)
                .sessionToken(sessionTokenEntity)
                .build();
    }

    public UserEntity login(UserEntity userEntity) {
        User savedUser = userService.get(userEntity.getEmail());
        if (savedUser == null) return null;
        boolean authResult = authenticationService.authenticatePassword(userEntity.getPassword(), savedUser.passwordDigest);
        if (authResult) {
            SessionToken savedSessionToken = sessionTokenService.getActive(savedUser.id);
            if (savedSessionToken == null) savedSessionToken = sessionTokenService.create(savedUser.id);
            SessionTokenEntity sessionTokenEntity = SessionTokenEntity.builder()
                    .token(savedSessionToken.token)
                    .build();
            return UserEntity.builder()
                    .email(savedUser.email)
                    .sessionToken(sessionTokenEntity)
                    .build();
        }
        return null;
    }

    public void logout(String token) {
        sessionTokenService.expire(token);
    }
}
