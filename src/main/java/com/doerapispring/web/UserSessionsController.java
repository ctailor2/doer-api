package com.doerapispring.web;

import com.doerapispring.authentication.AccessDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping(value = "/v1")
class UserSessionsController {
    private final HateoasLinkGenerator hateoasLinkGenerator;
    private final UserSessionsApiService userSessionsApiService;

    @Autowired
    UserSessionsController(HateoasLinkGenerator hateoasLinkGenerator, UserSessionsApiService userSessionsApiService) {
        this.hateoasLinkGenerator = hateoasLinkGenerator;
        this.userSessionsApiService = userSessionsApiService;
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.CREATED)
    @ResponseBody
    ResponseEntity<SessionResponse> signup(@RequestBody SignupForm signupForm) {
        try {
            SessionResponse sessionResponse = new SessionResponse(
                    userSessionsApiService.signup(signupForm.getIdentifier(),
                            signupForm.getCredentials()));
            sessionResponse.add(hateoasLinkGenerator.signupLink().withSelfRel(),
                    hateoasLinkGenerator.rootResourcesLink().withRel("root"));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(sessionResponse);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.OK)
    @ResponseBody
    ResponseEntity<SessionResponse> login(@RequestBody LoginForm loginForm) {
        try {
            SessionResponse sessionResponse = new SessionResponse(userSessionsApiService.login(loginForm.getIdentifier(),
                    loginForm.getCredentials()));
            sessionResponse.add(hateoasLinkGenerator.loginLink().withSelfRel(),
                    hateoasLinkGenerator.rootResourcesLink().withRel("root"));
            return ResponseEntity.ok()
                    .body(sessionResponse);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    // Logout doesn't really need to be an action taken against the server at all
    // If the client wants to terminate their current session, just have them drop their key
    // Clients shouldn't have to tell the server to stop allowing a key that it issued
    // If there are security concerns around key sharing, maybe the keys can be made ip specific or something
    // If the server wants credentials to be short lived, it can include an expiration
    // These are all things it can check when it receives a request with credentials in the header
}
