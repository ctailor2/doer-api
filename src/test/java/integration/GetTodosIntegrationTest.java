package integration;

import com.doerapispring.todos.TodoEntity;
import com.doerapispring.todos.TodoService;
import com.doerapispring.userSessions.UserSessionsService;
import com.doerapispring.users.UserEntity;
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

    private UserEntity user;

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
        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .password("password")
                .build();
        user = userSessionsService.signup(userEntity);
        httpHeaders.add("Session-Token", user.getSessionToken().getToken());
    }

    @Test
    public void todos_whenUserHasTodos_returnsTodos() throws Exception {
        TodoEntity todoEntity = TodoEntity.builder()
                .task("this and that")
                .build();
        todosService.create(user.getSessionToken().getToken(), todoEntity);

        doGet();

        MockHttpServletResponse response = mvcResult.getResponse();
        ObjectMapper mapper = new ObjectMapper();
        List<TodoEntity> savedTodoEntity = mapper.readValue(response.getContentAsString(), new TypeReference<List<TodoEntity>>() {
        });

        assertThat(savedTodoEntity.get(0).getTask()).isEqualTo("this and that");
    }
}
