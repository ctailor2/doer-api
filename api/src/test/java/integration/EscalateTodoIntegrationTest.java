package integration;

import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import com.jayway.jsonpath.JsonPath;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EscalateTodoIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {

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
    public void pull() throws Exception {
        String nextActionHref = JsonPath.parse(mockMvc.perform(get("/v1/lists/default")
                .headers(httpHeaders))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).read("$.list._links.unlock.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(post(nextActionHref)
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString()).read("$._links.list.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(get(nextActionHref)
                .headers(httpHeaders))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).read("$.list._links.create.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(post(nextActionHref)
                .content("{\"task\":\"will become deferred after the escalate\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString()).read("$.list._links.create.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(post(nextActionHref)
                .content("{\"task\":\"will remain\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString()).read("$.list._links.createDeferred.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(post(nextActionHref)
                .content("{\"task\":\"will no longer be deferred after the escalate\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString()).read("$.list._links.escalate.href", String.class);

        mockMvc.perform(post(nextActionHref)
            .headers(httpHeaders))
                .andExpect(jsonPath("$.list.todos[*].task", contains("will remain", "will no longer be deferred after the escalate")))
                .andExpect(jsonPath("$.list.deferredTodos[*].task", contains("will become deferred after the escalate")));
    }
}
