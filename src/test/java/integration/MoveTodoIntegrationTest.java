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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class MoveTodoIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private User user;
    private MvcResult mvcResult;

    private HttpHeaders httpHeaders = new HttpHeaders();

    @Autowired
    private UserSessionsApiService userSessionsApiService;
    @Autowired
    private TodoService todosService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        String identifier = "test@email.com";
        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier<>(identifier);
        user = new User(uniqueIdentifier);
        SessionTokenDTO signupSessionToken = userSessionsApiService.signup(identifier, "password");
        httpHeaders.add("Session-Token", signupSessionToken.getToken());
    }

    @Test
    public void move_movesTodos() throws Exception {
        todosService.create(user, "some task", ScheduledFor.later);
        todosService.create(user, "some other task", ScheduledFor.later);
        MasterList masterList = todosService.get(user);
        Todo firstTodo = masterList.getTodos().get(0);
        Todo secondTodo = masterList.getTodos().get(1);

        mvcResult = mockMvc.perform(post("/v1/todos/" + secondTodo.getLocalIdentifier() + "/move/" + firstTodo.getLocalIdentifier())
                .headers(httpHeaders))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        MasterList newMasterList = todosService.get(new User(new UniqueIdentifier<>("test@email.com")));

        assertThat(newMasterList.getTodos().get(0), equalTo(
                new Todo(secondTodo.getLocalIdentifier(),
                        secondTodo.getTask(),
                        secondTodo.getScheduling(),
                        firstTodo.getPosition())));
        assertThat(newMasterList.getTodos().get(1), equalTo(
                new Todo(firstTodo.getLocalIdentifier(),
                        firstTodo.getTask(),
                        firstTodo.getScheduling(),
                        secondTodo.getPosition())));
        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/todos/" + secondTodo.getLocalIdentifier() + "/move/" + firstTodo.getLocalIdentifier())));
        assertThat(responseContent, hasJsonPath("$._links.nowTodos.href", containsString("/v1/todos?scheduling=now")));
        assertThat(responseContent, hasJsonPath("$._links.laterTodos.href", containsString("/v1/todos?scheduling=later")));
        assertThat(responseContent, hasJsonPath("$._links.todoResources.href", containsString("/v1/resources/todo")));
    }
}
