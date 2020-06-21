package integration;

import com.doerapispring.domain.*;
import com.doerapispring.domain.events.TodoAddedEvent;
import com.doerapispring.domain.events.TodoCompletedEvent;
import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import com.jayway.jsonpath.JsonPath;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.text.IsEmptyString.isEmptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class GetCompletedListIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private final HttpHeaders httpHeaders = new HttpHeaders();

    @Autowired
    private UserSessionsApiService userSessionsApiService;

    @Autowired
    private TodoApplicationService todoApplicationService;

    @Autowired
    private ListApplicationService listApplicationService;

    @Autowired
    private UserService userService;

    private User user;

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
    public void list() throws Exception {
        listApplicationService.create(user, "someOtherList");
        listApplicationService.getAll(user).forEach(todoList -> {
            todoApplicationService.performOperation(user, todoList.getListId(), (todoId) -> new TodoAddedEvent(todoId.getIdentifier(), todoList.getName().concat(" task")), TodoListModel::applyEvent);
            Todo todo = TodoListModel.getTodos(listApplicationService.get(user, todoList.getListId())).head();
            todoApplicationService.performOperation(user, todoList.getListId(), () -> new TodoCompletedEvent(todo.getTodoId().getIdentifier()), TodoListModel::applyEvent);
        });

        MvcResult mvcResult = mockMvc.perform(get("/v1/lists/" + defaultListId.get() + "/completed")
            .headers(httpHeaders))
            .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();

        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$.list", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$.list.todos", hasSize(1)));
        assertThat(responseContent, hasJsonPath("$.list.todos[0].task", equalTo("default task")));
        String completedAtString = JsonPath.parse(responseContent).read("$.list.todos[0].completedAt", String.class);
        Assertions.assertThat(completedAtString).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\+\\d{4}");
        Date completedAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(completedAtString);
        Assertions.assertThat(completedAt).isToday();
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/lists/" + defaultListId.get() + "/completed")));
    }
}
