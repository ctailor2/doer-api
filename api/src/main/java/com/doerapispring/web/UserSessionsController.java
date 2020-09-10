package com.doerapispring.web;

import com.doerapispring.authentication.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@CrossOrigin
@RequestMapping(value = "/v1")
class UserSessionsController {
    private final HateoasLinkGenerator hateoasLinkGenerator;
    private final UserSessionsApiService userSessionsApiService;

    UserSessionsController(HateoasLinkGenerator hateoasLinkGenerator, UserSessionsApiService userSessionsApiService) {
        this.hateoasLinkGenerator = hateoasLinkGenerator;
        this.userSessionsApiService = userSessionsApiService;
    }

    @InitBinder("signupForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.setValidator(new SignupFormValidator());
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.CREATED)
    @ResponseBody
    ResponseEntity<SessionResponse> signup(@Valid @RequestBody SignupForm signupForm) throws AccessDeniedException {
        SessionTokenDTO sessionTokenDTO = userSessionsApiService.signup(
            signupForm.getIdentifier(),
            signupForm.getCredentials());
        SessionResponse sessionResponse = new SessionResponse(sessionTokenDTO);
        sessionResponse.add(
            hateoasLinkGenerator.signupLink().withSelfRel(),
            hateoasLinkGenerator.rootResourcesLink().withRel("root"));
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionResponse);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.OK)
    @ResponseBody
    ResponseEntity<SessionResponse> login(@RequestBody LoginForm loginForm) throws AccessDeniedException {
        SessionTokenDTO sessionTokenDTO = userSessionsApiService.login(
            loginForm.getIdentifier(),
            loginForm.getCredentials());
        SessionResponse sessionResponse = new SessionResponse(sessionTokenDTO);
        sessionResponse.add(
            hateoasLinkGenerator.loginLink().withSelfRel(),
            hateoasLinkGenerator.rootResourcesLink().withRel("root"));
        return ResponseEntity.ok().body(sessionResponse);
    }
}
