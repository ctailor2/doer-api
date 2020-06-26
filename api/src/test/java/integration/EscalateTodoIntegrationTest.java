package integration;

import com.doerapispring.domain.*;
import com.doerapispring.domain.events.DeferredTodoAddedEvent;
import com.doerapispring.domain.events.TodoAddedEvent;
import com.doerapispring.domain.events.UnlockedEvent;
import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Clock;

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

    @Autowired
    private Clock clock;

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
        todoApplicationService.performOperation(
                user,
                defaultListId,
                (todoId) -> new TodoAddedEvent(todoId.getIdentifier(), "will become deferred after the escalate"),
                TodoListModel::applyEvent);
        todoApplicationService.performOperation(
                user,
                defaultListId,
                (todoId) -> new TodoAddedEvent(todoId.getIdentifier(), "will remain"),
                TodoListModel::applyEvent);
        todoApplicationService.performOperation(
                user,
                defaultListId,
                (todoId) -> new DeferredTodoAddedEvent(todoId.getIdentifier(), "will no longer be deferred after the escalate"),
                TodoListModel::applyEvent);
        listApplicationService.performOperation(
                user,
                defaultListId,
                () -> new UnlockedEvent(java.util.Date.from(clock.instant())),
                TodoListModel::applyEvent);

        MvcResult mvcResult = mockMvc.perform(post("/v1/lists/" + defaultListId.get() + "/escalate")
            .headers(httpHeaders))
            .andReturn();
        String responseContent = mvcResult.getResponse().getContentAsString();

        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$.list.todos[*].task", contains("will remain", "will no longer be deferred after the escalate")));
        assertThat(responseContent, hasJsonPath("$.list.deferredTodos[*].task", contains("will become deferred after the escalate")));
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/lists/" + defaultListId.get() + "/escalate")));
        assertThat(responseContent, hasJsonPath("$._links.list.href", containsString("/v1/lists/" + defaultListId.get())));
    }
}
