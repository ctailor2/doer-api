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

public class EscalateTodoIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
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

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        String identifier = "test@email.com";
        SessionTokenDTO signupSessionToken = userSessionsApiService.signup(identifier, "password");
        user = userService.find(identifier).orElseThrow(RuntimeException::new);
        defaultListId = user.getDefaultListId();
        httpHeaders.add("Session-Token", signupSessionToken.getToken());
    }

    @Test
    public void pull() throws Exception {
        todoApplicationService.create(user, defaultListId, "will become deferred after the escalate");
        todoApplicationService.create(user, defaultListId, "will remain");
        todoApplicationService.createDeferred(user, defaultListId, "will no longer be deferred after the escalate");

        MvcResult mvcResult = mockMvc.perform(post("/v1/lists/" + defaultListId.get() + "/escalate")
            .headers(httpHeaders))
            .andReturn();
        String responseContent = mvcResult.getResponse().getContentAsString();
        listApplicationService.unlock(user, defaultListId);
        TodoListReadModel newTodoList = listApplicationService.get(user, defaultListId);

        Assertions.assertThat(newTodoList.getTodos()).extracting("task")
            .containsExactly("will remain", "will no longer be deferred after the escalate");
        Assertions.assertThat(newTodoList.getDeferredTodos()).extracting("task")
            .containsExactly("will become deferred after the escalate");
        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/lists/" + defaultListId.get() + "/escalate")));
        assertThat(responseContent, hasJsonPath("$._links.list.href", containsString("/v1/lists/" + defaultListId.get())));
    }
}
