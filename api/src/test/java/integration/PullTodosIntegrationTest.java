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
import scala.jdk.javaapi.CollectionConverters;

import java.time.Clock;
import java.util.Date;

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
        todoApplicationService.performOperation(user, defaultListId, (todoList, todoId) -> TodoListModel.addDeferred(todoList, todoId, "will get pulled"));
        todoApplicationService.performOperation(user, defaultListId, (todoList, todoId) -> TodoListModel.addDeferred(todoList, todoId, "will also get pulled"));
        todoApplicationService.performOperation(user, defaultListId, (todoList, todoId) -> TodoListModel.addDeferred(todoList, todoId, "keep for later"));

        MvcResult mvcResult = mockMvc.perform(post("/v1/lists/" + defaultListId.get() + "/pull")
            .headers(httpHeaders))
            .andReturn();
        String responseContent = mvcResult.getResponse().getContentAsString();
        listApplicationService.unlock(user, defaultListId);
        TodoListModel newTodoList = listApplicationService.get(user, defaultListId);

        Assertions.assertThat(CollectionConverters.asJava(TodoListModel.getTodos(newTodoList).map(Todo::getTask)))
            .containsExactly("will get pulled", "will also get pulled");
        Assertions.assertThat(CollectionConverters.asJava(TodoListModel.getDeferredTodos(newTodoList, Date.from(clock.instant())).map(Todo::getTask)))
            .containsExactly("keep for later");
        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/lists/" + defaultListId.get() + "/pull")));
        assertThat(responseContent, hasJsonPath("$._links.list.href", containsString("/v1/lists/" + defaultListId.get())));
    }
}
