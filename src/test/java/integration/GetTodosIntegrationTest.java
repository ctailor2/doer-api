package integration;

import com.doerapispring.todos.Todo;
import com.doerapispring.todos.TodoService;
import com.doerapispring.userSessions.UserSessionsService;
import com.doerapispring.users.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Created by chiragtailor on 9/27/16.
 */
public class GetTodosIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private MvcResult mvcResult;

    private HttpHeaders httpHeaders = new HttpHeaders();

    private User savedUser;

    @Autowired
    UserSessionsService userSessionsService;

    @Autowired
    TodoService todosService;

    private void doGet() throws Exception {
        mvcResult = mockMvc.perform(get("/v1/todos")
                .headers(httpHeaders))
                .andReturn();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        User user = User.builder()
                .email("test@email.com")
                .password("password")
                .build();
        savedUser = userSessionsService.signup(user);
        httpHeaders.add("Session-Token", savedUser.getSessionToken().getToken());
    }

    @Test
    public void todos_whenUserHasTodos_returnsTodos() throws Exception {
        Todo todo = Todo.builder()
                .task("this and that")
                .build();
        todosService.create(savedUser.getEmail(), todo);

        doGet();

        MockHttpServletResponse response = mvcResult.getResponse();
        ObjectMapper mapper = new ObjectMapper();
        List<Todo> savedTodo = mapper.readValue(response.getContentAsString(), new TypeReference<List<Todo>>() {
        });

        assertThat(savedTodo.get(0).getTask()).isEqualTo("this and that");
    }
}
