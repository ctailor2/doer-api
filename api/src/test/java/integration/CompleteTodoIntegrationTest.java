package integration;

import com.doerapispring.domain.*;
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
    public void complete_completesTodo() throws Exception {
        todosService.create(user, "some other task");
        todosService.create(user, "some task");
        ReadOnlyTodoList todoList = listService.get(user);
        Todo todo1 = todoList.getTodos().get(0);
        Todo todo2 = todoList.getTodos().get(1);

        MvcResult mvcResult = mockMvc.perform(post("/v1/todos/" + todo1.getTodoId().getIdentifier() + "/complete")
            .headers(httpHeaders))
            .andReturn();
        mockMvc.perform(post("/v1/todos/" + todo2.getTodoId().getIdentifier() + "/complete")
            .headers(httpHeaders))
            .andReturn();

        ReadOnlyTodoList newTodoList = listService.get(new User(new UserId("test@email.com")));
        ReadOnlyCompletedList newCompletedList = listService.getCompleted(new User(new UserId("test@email.com")));

        assertThat(newTodoList.getTodos(), hasSize(0));
        List<CompletedTodo> completedTodos = newCompletedList.getTodos();
        assertThat(completedTodos, hasSize(2));
        assertThat(completedTodos.get(0).getTask(), equalTo("some other task"));
        assertThat(completedTodos.get(1).getTask(), equalTo("some task"));
        String responseContent = mvcResult.getResponse().getContentAsString();
        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/todos/" + todo1.getTodoId().getIdentifier() + "/complete")));
        assertThat(responseContent, hasJsonPath("$._links.list.href", endsWith("/v1/list")));
    }
}
