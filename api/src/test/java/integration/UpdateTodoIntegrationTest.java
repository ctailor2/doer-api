package integration;

import com.doerapispring.domain.*;
import com.doerapispring.domain.events.TodoAddedEvent;
import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import scala.jdk.javaapi.CollectionConverters;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

public class UpdateTodoIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
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
        httpHeaders.add("Session-Token", signupSessionToken.getToken());
        user = userService.find(identifier).orElseThrow(RuntimeException::new);
        defaultListId = user.getDefaultListId();
    }

    @Test
    public void update() throws Exception {
        todoApplicationService.performOperation(
                user,
                defaultListId,
                (todoId) -> new TodoAddedEvent(todoId.getIdentifier(), "some task"),
                TodoListModel::applyEvent);
        TodoListModel todoList = listApplicationService.get(user, defaultListId);
        Todo todo = TodoListModel.getTodos(todoList).head();

        MvcResult mvcResult = mockMvc.perform(put("/v1/lists/" + defaultListId.get() + "/todos/" + todo.getTodoId().getIdentifier())
            .content("{\"task\":\"do the things\"}")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(httpHeaders))
            .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        TodoListModel newTodoList = listApplicationService.get(user, defaultListId);

        Assertions.assertThat(CollectionConverters.asJava(TodoListModel.getTodos(newTodoList).map(Todo::getTask)))
            .contains("do the things");
        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/lists/" + defaultListId.get() + "/todos/" + todo.getTodoId().getIdentifier())));
        assertThat(responseContent, hasJsonPath("$._links.list.href", endsWith("/v1/lists/" + defaultListId.get())));
    }
}
