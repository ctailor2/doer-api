package integration;

import com.doerapispring.domain.*;
import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import com.jayway.jsonpath.JsonPath;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URI;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MoveTodoIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private User user;

    private final HttpHeaders httpHeaders = new HttpHeaders();

    @Autowired
    private UserSessionsApiService userSessionsApiService;

    @Autowired
    private TodoService todosService;

    @Autowired
    private ListService listService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        String identifier = "test@email.com";
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>(identifier);
        user = new User(new UserId(uniqueIdentifier.get()));
        SessionTokenDTO signupSessionToken = userSessionsApiService.signup(identifier, "password");
        httpHeaders.add("Session-Token", signupSessionToken.getToken());
    }

    @Test
    public void move() throws Exception {
        mockMvc.perform(post("/v1/list/todos")
            .headers(httpHeaders)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"task\": \"some task\"}"))
            .andExpect(status().isCreated());
        mockMvc.perform(post("/v1/list/todos")
            .headers(httpHeaders)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"task\": \"some other task\"}"))
            .andExpect(status().isCreated());

        String todosResponse = mockMvc.perform(get("/v1/list")
            .headers(httpHeaders))
            .andExpect(jsonPath("$.list.todos", hasSize(2)))
            .andExpect(jsonPath("$.list.todos[0].task", equalTo("some other task")))
            .andExpect(jsonPath("$.list.todos[1].task", equalTo("some task")))
            .andReturn().getResponse().getContentAsString();
        String moveLink = JsonPath.parse(todosResponse).read("$.list.todos[0]._links.move[1].href", String.class);
        String movePath = URI.create(moveLink).getPath();

        MvcResult mvcResult = mockMvc.perform(post(movePath)
            .headers(httpHeaders))
            .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString(movePath)));
        assertThat(responseContent, hasJsonPath("$._links.list.href", endsWith("/v1/list")));

        mockMvc.perform(get("/v1/list")
            .headers(httpHeaders))
            .andExpect(jsonPath("$.list.todos", hasSize(2)))
            .andExpect(jsonPath("$.list.todos[0].task", equalTo("some task")))
            .andExpect(jsonPath("$.list.todos[1].task", equalTo("some other task")));
    }
}
