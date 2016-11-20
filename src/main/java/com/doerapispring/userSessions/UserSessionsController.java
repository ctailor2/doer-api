package com.doerapispring.userSessions;

import com.doerapispring.LoginForm;
import com.doerapispring.SignupForm;
import com.doerapispring.apiTokens.SessionToken;
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
    SessionToken signup(@RequestBody SignupForm signupForm) {
        return userSessionsService.signup(signupForm.getIdentifier(), signupForm.getCredentials());
    }

    // Resource - session (CRUD)
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.OK)
    @ResponseBody
    SessionToken login(@RequestBody LoginForm loginForm) {
        return userSessionsService.login(loginForm.getUserIdentifier(), loginForm.getCredentials());
    }

    // Logout doesn't really need to be an action taken against the server at all
    // If the client wants to terminate their current session, just have them drop their key
    // Clients shouldn't have to tell the server to stop allowing a key that it issued
    // If there are security concerns around key sharing, maybe the keys can be made ip specific or something
    // If the server wants credentials to be short lived, it can include an expiration
    // These are all things it can check when it receives a request with credentials in the header
}
