package com.doerapispring;

import com.doerapispring.userSessions.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by chiragtailor on 9/5/16.
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/v1")
public class PingAuthenticatedController {
    private final AuthenticationService authenticationService;

    @Autowired
    public PingAuthenticatedController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @RequestMapping(value = "/pingAuthenticated", method = RequestMethod.GET)
    ResponseEntity pingAuthenticated(@RequestHeader("Session-Token") String sessionToken) {
        if (!authenticationService.authenticateSessionToken(sessionToken)) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(HttpStatus.OK);
    }
}
