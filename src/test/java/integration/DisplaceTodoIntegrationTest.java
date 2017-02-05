package integration;

import com.doerapispring.domain.*;
import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class DisplaceTodoIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private User user;
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
        String identifier = "test@email.com";
        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier<>(identifier);
        user = new User(uniqueIdentifier);
        SessionTokenDTO signupSessionToken = userSessionsApiService.signup(identifier, "password");
        httpHeaders.add("Session-Token", signupSessionToken.getToken());
    }

    @Test
    public void displace_replacesImmediatelyScheduledTodo_bumpsItToPostponedList() throws Exception {
        todosService.create(user, "some task", ScheduledFor.now);
        MasterList masterList = todosService.get(user);
        Todo todo = masterList.getTodos().get(0);

        mvcResult = mockMvc.perform(post("/v1/todos/" + todo.getLocalIdentifier() + "/displace")
                .content("{\"task\":\"do the things\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        MasterList newMasterList = todosService.get(new User(new UniqueIdentifier<>("test@email.com")));

        assertThat(newMasterList.getTodos(), contains(
                new Todo(todo.getLocalIdentifier(), "do the things", ScheduledFor.now),
                new Todo("1", "some task", ScheduledFor.later)));
        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/todos/" + todo.getLocalIdentifier() + "/displace")));
        assertThat(responseContent, hasJsonPath("$._links.todos.href", containsString("/v1/todos")));
    }
}
