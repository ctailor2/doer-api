package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedAuthenticationToken;
import com.doerapispring.authentication.AuthenticatedUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static com.doerapispring.web.MockHateoasLinkGenerator.MOCK_BASE_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class HomeControllerTest {
    private MockMvc mockMvc;
    private HomeController homeController;
    private AuthenticatedUser authenticatedUser;

    @Mock
    TodoApiService mockTodoApiService;

    @Before
    public void setUp() throws Exception {
        String identifier = "test@email.com";
        authenticatedUser = new AuthenticatedUser(identifier);
        SecurityContextHolder.getContext().setAuthentication(new AuthenticatedAuthenticationToken(authenticatedUser));
        homeController = new HomeController(new MockHateoasLinkGenerator(), mockTodoApiService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(homeController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        when(mockTodoApiService.get(any())).thenReturn(new TodoListDTO(Collections.emptyList(), false));
    }

    @Test
    public void home_mapping() throws Exception {
        mockMvc.perform(get("/v1/home"))
                .andExpect(status().isOk());
    }

    @Test
    public void home_whenSchedulingForNowIsNotAllowed_includesLinks() throws Exception {
        ResponseEntity<HomeResponse> responseEntity = homeController.home(authenticatedUser);

        verify(mockTodoApiService).get(authenticatedUser);
        assertThat(responseEntity.getBody().getLinks()).containsOnly(
                new Link(MOCK_BASE_URL + "/home").withSelfRel(),
                new Link(MOCK_BASE_URL + "/todos").withRel("todos"),
                new Link(MOCK_BASE_URL + "/createTodoForLater").withRel("todoLater"),
                new Link(MOCK_BASE_URL + "/completedTodos").withRel("completedTodos"));
    }

    @Test
    public void home_whenSchedulingForNowIsAllowed_includesLink() throws Exception {
        when(mockTodoApiService.get(any())).thenReturn(new TodoListDTO(Collections.emptyList(), true));

        ResponseEntity<HomeResponse> responseEntity = homeController.home(authenticatedUser);

        verify(mockTodoApiService).get(authenticatedUser);
        assertThat(responseEntity.getBody().getLinks())
                .contains(new Link(MOCK_BASE_URL + "/createTodoForNow").withRel("todoNow"),
                        new Link(MOCK_BASE_URL + "/todos/pullTodos").withRel("pull"));
    }

    @Test
    public void home_whenInvalidRequest_throws400BadRequest() throws Exception {
        when(mockTodoApiService.get(any())).thenThrow(new InvalidRequestException());

        ResponseEntity<HomeResponse> responseEntity = homeController.home(authenticatedUser);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}