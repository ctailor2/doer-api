package integration;

import com.doerapispring.domain.*;
import com.doerapispring.domain.events.TodoAddedEvent;
import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static scala.jdk.javaapi.CollectionConverters.asJava;

public class DeleteTodoIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
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
    public void delete_removesTodo() throws Exception {
        todoApplicationService.performOperation(
                user,
                defaultListId,
                (todoId) -> new TodoAddedEvent(todoId.getIdentifier(), "some task"),
                TodoListModel::applyEvent);
        TodoListModel todoList = listApplicationService.get(user, defaultListId);
        Todo todo = TodoListModel.getTodos(todoList).head();

        MvcResult mvcResult = mockMvc.perform(delete("/v1/lists/" + defaultListId.get() + "/todos/" + todo.getTodoId().getIdentifier())
            .headers(httpHeaders))
            .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        TodoListModel newTodoList = listApplicationService.get(user, defaultListId);

        assertThat(asJava(TodoListModel.getTodos(newTodoList)), hasSize(0));
        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/lists/" + defaultListId.get() + "/todos/" + todo.getTodoId().getIdentifier())));
        assertThat(responseContent, hasJsonPath("$._links.list.href", endsWith("/v1/lists/" + defaultListId.get())));
    }
}
