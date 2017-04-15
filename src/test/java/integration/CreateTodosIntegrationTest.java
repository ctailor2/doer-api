package integration;

import com.doerapispring.domain.Todo;
import com.doerapispring.domain.TodoService;
import com.doerapispring.domain.UniqueIdentifier;
import com.doerapispring.domain.User;
import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class CreateTodosIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private MvcResult mvcResult;

    private HttpHeaders httpHeaders = new HttpHeaders();

    @Autowired
    UserSessionsApiService userSessionsApiService;

    @Autowired
    TodoService todosService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        SessionTokenDTO signupSessionToken = userSessionsApiService.signup("test@email.com", "password");
        httpHeaders.add("Session-Token", signupSessionToken.getToken());
    }

    @Test
    public void createForNow() throws Exception {
        mvcResult = mockMvc.perform(post("/v1/todoNow")
                .content("{\"task\":\"read the things\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
                .andReturn();

        List<Todo> todos = todosService.get(new User(new UniqueIdentifier<>("test@email.com"))).getTodos();

        assertThat(todos.size(), equalTo(1));

        String responseContent = mvcResult.getResponse().getContentAsString();
        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/todoNow")));
        assertThat(responseContent, hasJsonPath("$._links.todos.href", containsString("/v1/todos")));
        assertThat(responseContent, hasJsonPath("$._links.todos.href", containsString("/v1/todos")));
        assertThat(responseContent, hasJsonPath("$._links.todoResources.href", containsString("/v1/resources/todo")));
    }

    @Test
    public void createForLater() throws Exception {
        mvcResult = mockMvc.perform(post("/v1/todoLater")
                .content("{\"task\":\"read the things\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
                .andReturn();

        List<Todo> todos = todosService.get(new User(new UniqueIdentifier<>("test@email.com"))).getTodos();

        assertThat(todos.size(), equalTo(1));

        String responseContent = mvcResult.getResponse().getContentAsString();
        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/todoLater")));
        assertThat(responseContent, hasJsonPath("$._links.todos.href", containsString("/v1/todos")));
        assertThat(responseContent, hasJsonPath("$._links.todoResources.href", containsString("/v1/resources/todo")));
    }
}
