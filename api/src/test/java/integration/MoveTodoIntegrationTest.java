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

    @Autowired
    private ListService listService;

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
    public void move() throws Exception {
        todosService.createDeferred(user, "some task");
        todosService.createDeferred(user, "some other task");
        listService.unlock(user);
        MasterList masterList = todosService.get(user);
        Todo firstTodo = masterList.getDeferredTodos().get(0);
        Todo secondTodo = masterList.getDeferredTodos().get(1);

        mvcResult = mockMvc.perform(post("/v1/todos/" + secondTodo.getLocalIdentifier() + "/move/" + firstTodo.getLocalIdentifier())
                .headers(httpHeaders))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        MasterList newMasterList = todosService.get(new User(new UniqueIdentifier<>("test@email.com")));

        assertThat(newMasterList.getDeferredTodos().get(0), equalTo(
                new Todo(secondTodo.getLocalIdentifier(),
                        secondTodo.getTask(),
                        secondTodo.getListName(),
                        firstTodo.getPosition())));
        assertThat(newMasterList.getDeferredTodos().get(1), equalTo(
                new Todo(firstTodo.getLocalIdentifier(),
                        firstTodo.getTask(),
                        firstTodo.getListName(),
                        secondTodo.getPosition())));
        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/todos/" + secondTodo.getLocalIdentifier() + "/move/" + firstTodo.getLocalIdentifier())));
        assertThat(responseContent, hasJsonPath("$._links.list.href", endsWith("/v1/list")));
    }
}