package integration;

import com.doerapispring.domain.*;
import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.text.IsEmptyString.isEmptyString;

@ActiveProfiles(value = "test")
public class GetTodosIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private MvcResult mvcResult;
    private HttpHeaders httpHeaders = new HttpHeaders();
    private MockHttpServletRequestBuilder baseMockRequestBuilder;
    private MockHttpServletRequestBuilder mockRequestBuilder;
    private User user;

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
        baseMockRequestBuilder = MockMvcRequestBuilders
                .get("/v1/todos")
                .headers(httpHeaders);
    }

    @Test
    public void todos_whenUserHasTodos_returnsAllTodos() throws Exception {
        mockRequestBuilder = baseMockRequestBuilder;
        todosService.create(user, "this and that", ScheduledFor.later);
        todosService.create(user, "here and now", ScheduledFor.later);
        MasterList masterList = todosService.get(user);
        Todo firstTodo = masterList.getTodos().get(0);
        Todo secondTodo = masterList.getTodos().get(1);

        doGet();

        String responseContent = mvcResult.getResponse().getContentAsString();

        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$.todos", hasSize(2)));
        assertThat(responseContent, hasJsonPath("$.todos[0].task", equalTo("this and that")));
        assertThat(responseContent, hasJsonPath("$.todos[0].scheduling", equalTo("later")));
        assertThat(responseContent, hasJsonPath("$.todos[0]._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$.todos[0]._links.delete.href", containsString("v1/todos/" + firstTodo.getLocalIdentifier())));
        assertThat(responseContent, hasJsonPath("$.todos[0]._links.update.href", containsString("v1/todos/" + firstTodo.getLocalIdentifier())));
        assertThat(responseContent, hasJsonPath("$.todos[0]._links.complete.href", containsString("v1/todos/" + firstTodo.getLocalIdentifier() + "/complete")));
        assertThat(responseContent, hasJsonPath("$.todos[0]._links.move", hasSize(2)));
        assertThat(responseContent, hasJsonPath("$.todos[0]._links.move[0].href", containsString("v1/todos/" + firstTodo.getLocalIdentifier() + "/move/" + firstTodo.getLocalIdentifier())));
        assertThat(responseContent, hasJsonPath("$.todos[0]._links.move[1].href", containsString("v1/todos/" + firstTodo.getLocalIdentifier() + "/move/" + secondTodo.getLocalIdentifier())));
        assertThat(responseContent, hasJsonPath("$.todos[1]._links.move[0].href", containsString("v1/todos/" + secondTodo.getLocalIdentifier() + "/move/" + firstTodo.getLocalIdentifier())));
        assertThat(responseContent, hasJsonPath("$.todos[1]._links.move[1].href", containsString("v1/todos/" + secondTodo.getLocalIdentifier() + "/move/" + secondTodo.getLocalIdentifier())));
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/todos")));
        assertThat(responseContent, hasJsonPath("$._links.todoNow.href", containsString("/v1/todoNow")));
        assertThat(responseContent, hasJsonPath("$._links.todoLater.href", containsString("/v1/todoLater")));
    }

    private void doGet() throws Exception {
        mvcResult = mockMvc.perform(mockRequestBuilder)
                .andReturn();
    }
}
