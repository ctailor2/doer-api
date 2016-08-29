package com.doerapispring.userSessions;

import com.doerapispring.apiTokens.SessionToken;
import com.doerapispring.apiTokens.SessionTokenEntity;
import com.doerapispring.apiTokens.SessionTokenService;
import com.doerapispring.users.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Created by chiragtailor on 8/11/16.
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/v1")
class UserSessionsController {
    private UserService userService;
    private SessionTokenService sessionTokenService;
    private AuthenticationService authenticationService;

    @Autowired
    UserSessionsController(UserService userService, SessionTokenService sessionTokenService, AuthenticationService authenticationService) {
        this.userService = userService;
        this.sessionTokenService = sessionTokenService;
        this.authenticationService = authenticationService;
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.CREATED)
    @ResponseBody
    UserSessionResponseWrapper signup(@RequestBody UserSessionRequestWrapper userSessionRequestWrapper) {
        UserEntity userEntity = userSessionRequestWrapper.getUser();
        User savedUser = userService.create(userEntity);
        SessionToken savedSessionToken = sessionTokenService.create(savedUser.id);
        return UserSessionResponseWrapper.builder()
                .user(UserEntity.builder().email(savedUser.email).build())
                .sessionToken(SessionTokenEntity.builder().token(savedSessionToken.token).build())
                .build();
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.OK)
    @ResponseBody
    UserSessionResponseWrapper login(@RequestBody UserSessionRequestWrapper userSessionRequestWrapper) {
        UserEntity userEntity = userSessionRequestWrapper.getUser();
        User savedUser = userService.get(userEntity.getEmail());
        boolean authResult = authenticationService.authenticate(userEntity.getPassword(), savedUser.passwordDigest);
        UserSessionResponseWrapper.UserSessionResponseWrapperBuilder responseBuilder = UserSessionResponseWrapper.builder();
        responseBuilder.user(UserEntity.builder().email(savedUser.email).build());
        if (authResult) {
            SessionToken savedSessionToken = sessionTokenService.create(savedUser.id);
            responseBuilder.sessionToken(SessionTokenEntity.builder().token(savedSessionToken.token).build());
        }
        return responseBuilder.build();
    }
}
