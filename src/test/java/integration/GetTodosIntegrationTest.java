package integration;

import com.doerapispring.Credentials;
import com.doerapispring.UserIdentifier;
import com.doerapispring.apiTokens.SessionToken;
import com.doerapispring.todos.Todo;
import com.doerapispring.todos.TodoService;
import com.doerapispring.userSessions.UserSessionsService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by chiragtailor on 9/27/16.
 */
public class GetTodosIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private MvcResult mvcResult;

    private HttpHeaders httpHeaders = new HttpHeaders();

    @Autowired
    UserSessionsService userSessionsService;

    @Autowired
    TodoService todosService;


    private MockHttpServletRequestBuilder baseMockRequestBuilder;
    private MockHttpServletRequestBuilder mockRequestBuilder;

    private void doGet() throws Exception {
        mvcResult = mockMvc.perform(mockRequestBuilder)
                .andReturn();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        SessionToken signupSessionToken = userSessionsService.signup(
                new UserIdentifier("test@email.com"),
                new Credentials("password"));
        httpHeaders.add("Session-Token", signupSessionToken.getToken());
        baseMockRequestBuilder = MockMvcRequestBuilders
                .get("/v1/todos")
                .headers(httpHeaders);
    }

    @Test
    public void todos_whenUserHasTodos_returnsAllTodos() throws Exception {
        mockRequestBuilder = baseMockRequestBuilder;
        Todo todo = Todo.builder()
                .task("this and that")
                .build();
        todosService.create("test@email.com", todo);

        doGet();

        MockHttpServletResponse response = mvcResult.getResponse();
        ObjectMapper mapper = new ObjectMapper();
        List<Todo> savedTodos = mapper.readValue(response.getContentAsString(), new TypeReference<List<Todo>>() {
        });

        assertThat(savedTodos.size()).isEqualTo(1);
        Todo savedTodo = savedTodos.get(0);
        assertThat(savedTodo.getTask()).isEqualTo("this and that");
        assertThat(savedTodo.isActive()).isEqualTo(false);
    }

    @Test
    public void todos_whenUserHasTodos_withQueryForActive_returnsActiveTodos() throws Exception {
        mockRequestBuilder = baseMockRequestBuilder.param("type", "active");
        Todo todo1 = Todo.builder()
                .task("active task")
                .active(true)
                .build();
        Todo todo2 = Todo.builder()
                .task("inactive task")
                .active(false)
                .build();
        todosService.create("test@email.com", todo1);
        todosService.create("test@email.com", todo2);

        doGet();

        MockHttpServletResponse response = mvcResult.getResponse();
        ObjectMapper mapper = new ObjectMapper();
        List<Todo> savedTodos = mapper.readValue(response.getContentAsString(), new TypeReference<List<Todo>>() {
        });

        assertThat(savedTodos.size()).isEqualTo(1);
        Todo savedTodo = savedTodos.get(0);
        assertThat(savedTodo.getTask()).isEqualTo("active task");
        assertThat(savedTodo.isActive()).isEqualTo(true);
    }
}
