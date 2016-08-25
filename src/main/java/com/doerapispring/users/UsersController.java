package com.doerapispring.users;

import com.doerapispring.apiTokens.SessionTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Created by chiragtailor on 8/11/16.
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/v1")
class UsersController {
    private UserService userService;
    private SessionTokenService sessionTokenService;

    @Autowired
    public UsersController(UserService userService, SessionTokenService sessionTokenService) {
        this.userService = userService;
        this.sessionTokenService = sessionTokenService;
    }

    @RequestMapping(value = "/users", method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.CREATED)
    public @ResponseBody UserResponseWrapper create(@RequestBody UserRequestWrapper userRequestWrapper) {
        UserEntity userEntity = userRequestWrapper.getUser();
        User savedUser = userService.create(userEntity);
        sessionTokenService.create(savedUser.id);
        return UserResponseWrapper.builder().user(userEntity).build();
    }
}
