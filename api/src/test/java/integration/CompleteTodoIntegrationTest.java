package integration;

import com.doerapispring.domain.*;
import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class CompleteTodoIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
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
    public void complete_completesTodo() throws Exception {
        todosService.create(user, "some task");
        MasterList masterList = todosService.get(user);
        Todo todo = masterList.getTodos().get(0);

        mvcResult = mockMvc.perform(post("/v1/todos/" + todo.getLocalIdentifier() + "/complete")
                .headers(httpHeaders))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        MasterList newMasterList = todosService.get(new User(new UniqueIdentifier<>("test@email.com")));
        CompletedList newCompletedList = todosService.getCompleted(new User(new UniqueIdentifier<>("test@email.com")));

        assertThat(newMasterList.getTodos(), hasSize(0));
        List<CompletedTodo> completedTodos = newCompletedList.getTodos();
        assertThat(completedTodos, hasSize(1));
        assertThat(completedTodos.get(0).getTask(), equalTo("some task"));
        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/todos/" + todo.getLocalIdentifier() + "/complete")));
        assertThat(responseContent, hasJsonPath("$._links.list.href", endsWith("/v1/list")));
    }
}