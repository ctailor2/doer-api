package integration;

import com.doerapispring.domain.*;
import com.doerapispring.domain.events.DeprecatedTodoAddedEvent;
import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class CompleteTodoIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
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
    public void complete_completesTodo() throws Exception {
        todoApplicationService.performOperation(
                user,
                defaultListId,
                (todoId) -> new DeprecatedTodoAddedEvent(todoId.getIdentifier(), "some other task"),
                DeprecatedTodoListModel::applyEvent);
        todoApplicationService.performOperation(
                user,
                defaultListId,
                (todoId) -> new DeprecatedTodoAddedEvent(todoId.getIdentifier(), "some task"),
                DeprecatedTodoListModel::applyEvent);
        DeprecatedTodoListModel todoList = listApplicationService.get(user, defaultListId);
        DeprecatedTodo todo1 = DeprecatedTodoListModel.getTodos(todoList).head();
        DeprecatedTodo todo2 = DeprecatedTodoListModel.getTodos(todoList).last();

        mockMvc.perform(post("/v1/lists/" + defaultListId.get() + "/todos/" + todo1.getTodoId().getIdentifier() + "/complete")
                .headers(httpHeaders))
                .andReturn();
        MvcResult mvcResult = mockMvc.perform(post("/v1/lists/" + defaultListId.get() + "/todos/" + todo2.getTodoId().getIdentifier() + "/complete")
                .headers(httpHeaders))
                .andReturn();

        List<CompletedTodo> completedTodos = listApplicationService.getCompleted(user, defaultListId).getTodos();
        assertThat(completedTodos, hasSize(2));
        assertThat(completedTodos.get(0).getTask(), equalTo("some other task"));
        assertThat(completedTodos.get(1).getTask(), equalTo("some task"));
        String responseContent = mvcResult.getResponse().getContentAsString();
        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$.list.todos", hasSize(0)));
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/lists/" + defaultListId.get() + "/todos/" + todo2.getTodoId().getIdentifier() + "/complete")));
        assertThat(responseContent, hasJsonPath("$._links.list.href", containsString("/v1/lists/" + defaultListId.get())));
    }
}
