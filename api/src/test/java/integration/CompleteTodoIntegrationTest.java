package integration;

import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import com.jayway.jsonpath.JsonPath;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CompleteTodoIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {

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
    public void complete_completesTodo() throws Exception {
        String nextActionHref = JsonPath.parse(mockMvc.perform(get("/v1/lists/default")
                .headers(httpHeaders))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).read("$.list._links.create.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(post(nextActionHref)
                .content("{\"task\": \"some other task\"}")
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString()).read("$.list._links.create.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(post(nextActionHref)
                .content("{\"task\": \"some task\"}")
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString()).read("$.list.todos[0]._links.complete.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(post(nextActionHref)
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString()).read("$.list.todos[0]._links.complete.href", String.class);
        String contentAsString = mockMvc.perform(post(nextActionHref)
                .headers(httpHeaders))
                .andExpect(jsonPath("$.list.todos", hasSize(0)))
                .andReturn().getResponse().getContentAsString();
        nextActionHref = JsonPath.parse(contentAsString).read("$.list._links.completed.href");

        mockMvc.perform(get(nextActionHref)
                .headers(httpHeaders))
                .andExpect(jsonPath("$.list.todos", hasSize(2)))
                .andExpect(jsonPath("$.list.todos[0].task", equalTo("some other task")))
                .andExpect(jsonPath("$.list.todos[1].task", equalTo("some task")));
    }
}
