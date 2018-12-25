package integration;

import com.doerapispring.domain.*;
import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class CreateTodosIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private MvcResult mvcResult;

    private final HttpHeaders httpHeaders = new HttpHeaders();

    @Autowired
    private UserSessionsApiService userSessionsApiService;

    @Autowired
    private TodoApplicationService todoApplicationService;

    @Autowired
    private ListApplicationService listApplicationService;
    private ListId defaultListId;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        String identifier = "test@email.com";
        User user = new User(new UserId(identifier));
        SessionTokenDTO signupSessionToken = userSessionsApiService.signup(identifier, "password");
        defaultListId = listApplicationService.get(user).getListId();
        httpHeaders.add("Session-Token", signupSessionToken.getToken());
    }

    @Test
    public void createForNow() throws Exception {
        mvcResult = mockMvc.perform(post("/v1/lists/" + defaultListId.get() + "/todos")
            .content("{\"task\":\"read the things\"}")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(httpHeaders))
            .andReturn();

        List<Todo> todos = listApplicationService.get(new User(new UserId("test@email.com"))).getTodos();

        assertThat(todos.size(), equalTo(1));

        String responseContent = mvcResult.getResponse().getContentAsString();
        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/lists/" + defaultListId.get() + "/todos")));
        assertThat(responseContent, hasJsonPath("$._links.list.href", containsString("/v1/lists/" + defaultListId.get())));
    }

    @Test
    public void createForLater() throws Exception {
        mvcResult = mockMvc.perform(post("/v1/lists/" + defaultListId.get() + "/deferredTodos")
            .content("{\"task\":\"read the things\"}")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(httpHeaders))
            .andReturn();

        User user = new User(new UserId("test@email.com"));
        listApplicationService.unlock(user);
        List<Todo> todos = listApplicationService.get(user).getDeferredTodos();

        assertThat(todos.size(), equalTo(1));

        String responseContent = mvcResult.getResponse().getContentAsString();
        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/lists/" + defaultListId.get() + "/deferredTodos")));
        assertThat(responseContent, hasJsonPath("$._links.list.href", containsString("/v1/lists/" + defaultListId.get())));
    }
}
