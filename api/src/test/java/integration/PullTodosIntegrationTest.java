package integration;

import com.doerapispring.domain.*;
import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class PullTodosIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private User user;

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
        user = new User(new UserId(identifier));
        SessionTokenDTO signupSessionToken = userSessionsApiService.signup(identifier, "password");
        defaultListId = listApplicationService.get(user).getListId();
        httpHeaders.add("Session-Token", signupSessionToken.getToken());
    }

    @Test
    public void pull() throws Exception {
        todoApplicationService.createDeferred(user, defaultListId, "will get pulled");
        todoApplicationService.createDeferred(user, defaultListId, "will also get pulled");
        todoApplicationService.createDeferred(user, defaultListId, "keep for later");

        MvcResult mvcResult = mockMvc.perform(post("/v1/lists/" + defaultListId.get() + "/pull")
            .headers(httpHeaders))
            .andReturn();
        String responseContent = mvcResult.getResponse().getContentAsString();
        User user = new User(new UserId("test@email.com"));
        listApplicationService.unlock(user);
        ReadOnlyTodoList newTodoList = listApplicationService.get(user);

        Assertions.assertThat(newTodoList.getTodos()).extracting("task")
            .containsExactly("will get pulled", "will also get pulled");
        Assertions.assertThat(newTodoList.getDeferredTodos()).extracting("task")
            .containsExactly("keep for later");
        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/lists/" + defaultListId.get() + "/pull")));
        assertThat(responseContent, hasJsonPath("$._links.list.href", containsString("/v1/lists/" + defaultListId.get())));
    }
}
