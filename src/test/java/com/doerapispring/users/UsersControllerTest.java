package com.doerapispring.users;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

/**
 * Created by chiragtailor on 8/12/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class UsersControllerTest {
    private UsersController usersController;

    @Mock
    private UserService userService;

    @Before
    public void setUp() throws Exception {
        usersController = new UsersController(userService);
    }

    @Test
    public void create_callsUserService_create_withUserEntity() throws Exception {
        UserEntity userEntity = UserEntity.builder().build();
        UserRequestWrapper userRequestWrapper = UserRequestWrapper.builder().user(userEntity).build();

        usersController.create(userRequestWrapper);
        verify(userService).create(userEntity);
    }
}