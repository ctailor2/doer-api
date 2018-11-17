package integration;

import com.doerapispring.domain.*;
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

public class DeleteTodoIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
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
    public void delete_removesTodo() throws Exception {
        todosService.create(user, "some task");
        ReadOnlyTodoList todoList = listService.get(user);
        Todo todo = todoList.getTodos().get(0);

        MvcResult mvcResult = mockMvc.perform(delete("/v1/todos/" + todo.getTodoId().getIdentifier())
            .headers(httpHeaders))
            .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        ReadOnlyTodoList newTodoList = listService.get(new User(new UserId("test@email.com")));

        assertThat(newTodoList.getTodos(), hasSize(0));
        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/todos/" + todo.getTodoId().getIdentifier())));
        assertThat(responseContent, hasJsonPath("$._links.list.href", endsWith("/v1/list")));
    }
}
