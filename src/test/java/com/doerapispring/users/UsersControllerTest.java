package com.doerapispring.users;

import com.doerapispring.apiTokens.SessionTokenService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

/**
 * Created by chiragtailor on 8/12/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class UsersControllerTest {
    private UsersController usersController;

    private UserEntity userEntity = UserEntity.builder().build();
    private UserRequestWrapper userRequestWrapper = UserRequestWrapper.builder().user(userEntity).build();
    private User savedUser = User.builder().id(1L).build();

    @Mock
    private UserService userService;

    @Mock
    private SessionTokenService sessionTokenService;

    @Before
    public void setUp() throws Exception {
        doReturn(savedUser).when(userService).create(userEntity);
        usersController = new UsersController(userService, sessionTokenService);
    }

    @Test
    public void create_callsUserService_create_withUserEntity() throws Exception {
        usersController.create(userRequestWrapper);
        verify(userService).create(userEntity);
    }

    @Test
    public void create_callsSessionTokenService_create_withSavedUserId() throws Exception {
        usersController.create(userRequestWrapper);
        verify(sessionTokenService).create(1L);
    }
}