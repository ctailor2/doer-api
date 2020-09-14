package integration;

import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import com.jayway.jsonpath.JsonPath;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MoveTodoIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private final HttpHeaders httpHeaders = new HttpHeaders();

    @Autowired
    private UserSessionsApiService userSessionsApiService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        String identifier = "test@email.com";
        SessionTokenDTO signupSessionToken = userSessionsApiService.signup(identifier, "password");
        httpHeaders.add("Session-Token", signupSessionToken.getToken());
    }

    @Test
    public void move() throws Exception {
        String nextActionHref = JsonPath.parse(mockMvc.perform(get("/v1/lists/default")
                .headers(httpHeaders))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).read("$.list._links.create.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(post(nextActionHref)
                .content("{\"task\":\"some task\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString()).read("$.list._links.create.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(post(nextActionHref)
                .content("{\"task\":\"some other task\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
            .andExpect(jsonPath("$.list.todos", hasSize(2)))
            .andExpect(jsonPath("$.list.todos[0].task", equalTo("some other task")))
            .andExpect(jsonPath("$.list.todos[1].task", equalTo("some task")))
            .andReturn().getResponse().getContentAsString()).read("$.list.todos[0]._links.move[1].href", String.class);

        mockMvc.perform(post(nextActionHref)
            .headers(httpHeaders))
                .andExpect(jsonPath("$.list.todos[0].task", equalTo("some task")))
                .andExpect(jsonPath("$.list.todos[1].task", equalTo("some other task")))
                .andExpect(jsonPath("$._links", not(isEmptyString())))
                .andExpect(jsonPath("$._links.self.href", equalTo(nextActionHref)));
    }
}
