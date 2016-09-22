package com.doerapispring.userSessions;

import com.doerapispring.users.UserEntity;
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
    private UserSessionsService userSessionsService;

    @Autowired
    UserSessionsController(UserSessionsService userSessionsService) {
        this.userSessionsService = userSessionsService;
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.CREATED)
    @ResponseBody
    UserEntity signup(@RequestBody UserEntity userEntity) {
        return userSessionsService.signup(userEntity);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.OK)
    @ResponseBody
    UserEntity login(@RequestBody UserEntity userEntity) {
        return userSessionsService.login(userEntity);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.OK)
    void logout(@RequestHeader(value = "Session-Token") String token) {
        userSessionsService.logout(token);
    }
}
