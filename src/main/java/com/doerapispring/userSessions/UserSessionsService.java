package com.doerapispring.userSessions;

import com.doerapispring.apiTokens.SessionTokenEntity;
import com.doerapispring.apiTokens.SessionTokenService;
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
        UserEntity savedUserEntity = userService.create(userEntity);
        SessionTokenEntity sessionTokenEntity = sessionTokenService.create(savedUserEntity.getEmail());
        savedUserEntity.setSessionToken(sessionTokenEntity);
        return savedUserEntity;
    }

    public UserEntity login(UserEntity userEntity) {
        boolean authResult = authenticationService.authenticate(userEntity.getEmail(), userEntity.getPassword());
        if (!authResult) return null;
        SessionTokenEntity sessionTokenEntity = sessionTokenService.getActive(userEntity.getEmail());
        if (sessionTokenEntity == null) sessionTokenEntity = sessionTokenService.create(userEntity.getEmail());
        userEntity.setSessionToken(sessionTokenEntity);
        return userEntity;
    }

    public void logout(String token) {
        sessionTokenService.expire(token);
    }
}
