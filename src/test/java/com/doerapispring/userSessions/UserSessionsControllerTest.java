package com.doerapispring.userSessions;

import com.doerapispring.apiTokens.SessionToken;
import com.doerapispring.apiTokens.SessionTokenService;
import com.doerapispring.users.User;
import com.doerapispring.users.UserEntity;
import com.doerapispring.users.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by chiragtailor on 8/12/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserSessionsControllerTest {
    private UserSessionsController userSessionsController;

    private UserEntity userEntity = UserEntity.builder().build();
    private UserSessionRequestWrapper userSessionRequestWrapper = UserSessionRequestWrapper.builder().user(userEntity).build();
    private User savedUser = User.builder().id(1L).email("test@email.com").build();
    private SessionToken savedSessionToken = SessionToken.builder().build();

    @Mock
    private UserService userService;

    @Mock
    private SessionTokenService sessionTokenService;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        doReturn(savedUser).when(userService).create(any(UserEntity.class));
        doReturn(savedSessionToken).when(sessionTokenService).create(savedUser.id);
        userSessionsController = new UserSessionsController(userService, sessionTokenService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(userSessionsController)
                .build();
    }

    @Test
    public void signup_mapping() throws Exception {
        mockMvc.perform(post("/v1/signup")
                .accept(MediaType.APPLICATION_JSON)
                .content("{}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    public void create_callsUserService_create_withUserEntity() throws Exception {
        userSessionsController.create(userSessionRequestWrapper);
        verify(userService).create(userEntity);
    }

    @Test
    public void create_callsSessionTokenService_create_withSavedUserId() throws Exception {
        userSessionsController.create(userSessionRequestWrapper);
        verify(sessionTokenService).create(1L);
    }
}