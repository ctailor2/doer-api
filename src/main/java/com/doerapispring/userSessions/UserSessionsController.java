package com.doerapispring.userSessions;

import com.doerapispring.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    User signup(@RequestBody User user) {
        return userSessionsService.signup(user);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.OK)
    @ResponseBody
    User login(@RequestBody User user) {
        return userSessionsService.login(user);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.OK)
    void logout(@AuthenticationPrincipal String userEmail) {
        userSessionsService.logout(userEmail);
    }
}
