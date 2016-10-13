package integration;

import com.doerapispring.todos.Todo;
import com.doerapispring.todos.TodoService;
import com.doerapispring.userSessions.UserSessionsService;
import com.doerapispring.users.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Created by chiragtailor on 10/5/16.
 */
public class CreateTodosIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private MvcResult mvcResult;

    private HttpHeaders httpHeaders = new HttpHeaders();

    @Autowired
    UserSessionsService userSessionsService;

    @Autowired
    TodoService todosService;

    private User savedUser;

    private void doPost() throws Exception {
        mvcResult = mockMvc.perform(post("/v1/todos")
                .content("{\n" +
                        "  \"task\":\"read the things\",\n" +
                        "  \"active\":true\n" +
                        "}")
                .contentType(MediaType.APPLICATION_JSON)
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
    public void create() throws Exception {
        doPost();

        List<Todo> todos = todosService.get(savedUser.getEmail());

        MockHttpServletResponse response = mvcResult.getResponse();
        ObjectMapper mapper = new ObjectMapper();
        Todo savedTodo = mapper.readValue(response.getContentAsString(), Todo.class);
        assertThat(savedTodo.getTask()).isEqualTo("read the things");
        assertThat(savedTodo.isActive()).isEqualTo(true);
        assertThat(todos.size()).isEqualTo(1);
        assertThat(todos.get(0)).isEqualTo(savedTodo);
    }
}
