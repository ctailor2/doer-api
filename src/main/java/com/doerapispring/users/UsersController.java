package com.doerapispring.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Created by chiragtailor on 8/11/16.
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/v1")
public class UsersController {
    private UserService userService;

    @Autowired
    public UsersController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/users", method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.CREATED)
    public @ResponseBody UserResponseWrapper create(@RequestBody UserRequestWrapper userRequestWrapper) {
        UserEntity userEntity = userRequestWrapper.getUser();
        userService.create(userEntity);
        UserResponseWrapper userResponseWrapper = UserResponseWrapper.builder().user(userEntity).build();
        return userResponseWrapper;
    }
}
