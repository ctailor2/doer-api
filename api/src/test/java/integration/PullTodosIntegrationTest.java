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

import java.time.Clock;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PullTodosIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private User user;

    private final HttpHeaders httpHeaders = new HttpHeaders();

    @Autowired
    private UserSessionsApiService userSessionsApiService;

    @Autowired
    private TodoApplicationService todoApplicationService;

    @Autowired
    private ListApplicationService listApplicationService;

    @Autowired
    private UserService userService;

    private ListId defaultListId;

    @Autowired
    private Clock clock;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        String identifier = "test@email.com";
        SessionTokenDTO signupSessionToken = userSessionsApiService.signup(identifier, "password");
        httpHeaders.add("Session-Token", signupSessionToken.getToken());
        user = userService.find(identifier).orElseThrow(RuntimeException::new);
        defaultListId = user.getDefaultListId();
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
                .andReturn().getResponse().getContentAsString()).read("$.list._links.createDeferred.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(post(nextActionHref)
                .content("{\"task\":\"will get pulled\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString()).read("$.list._links.createDeferred.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(post(nextActionHref)
                .content("{\"task\":\"will also get pulled\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString()).read("$.list._links.createDeferred.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(post(nextActionHref)
                .content("{\"task\":\"keep for later\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString()).read("$.list._links.pull.href", String.class);
        mockMvc.perform(post(nextActionHref)
                .headers(httpHeaders))
                .andExpect(jsonPath("$.list.todos[*].task", contains("will get pulled", "will also get pulled")))
                .andExpect(jsonPath("$.list.deferredTodos[*].task", contains("keep for later")));
    }
}
