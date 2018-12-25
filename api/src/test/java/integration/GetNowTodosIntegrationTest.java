package integration;

import com.doerapispring.domain.*;
import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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

public class GetNowTodosIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private MvcResult mvcResult;
    private final HttpHeaders httpHeaders = new HttpHeaders();
    private MockHttpServletRequestBuilder baseMockRequestBuilder;
    private MockHttpServletRequestBuilder mockRequestBuilder;
    private User user;

    @Autowired
    private UserSessionsApiService userSessionsApiService;

    @Autowired
    private TodoApplicationService todoApplicationService;

    @Autowired
    private ListApplicationService listApplicationService;
    private ListId defaultListId;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        String identifier = "test@email.com";
        user = new User(new UserId(identifier));
        SessionTokenDTO signupSessionToken = userSessionsApiService.signup(identifier, "password");
        defaultListId = listApplicationService.get(user).getListId();
        httpHeaders.add("Session-Token", signupSessionToken.getToken());
        baseMockRequestBuilder = MockMvcRequestBuilders
                .get("/v1/lists/" + defaultListId.get())
                .headers(httpHeaders);
    }

    @Test
    public void todos() throws Exception {
        mockRequestBuilder = baseMockRequestBuilder;
        todoApplicationService.create(user, defaultListId, "this and that");
        ReadOnlyTodoList todoList = listApplicationService.get(user);
        Todo firstTodo = todoList.getTodos().get(0);

        doGet();

        String responseContent = mvcResult.getResponse().getContentAsString();

        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$.list.todos", hasSize(1)));
        assertThat(responseContent, hasJsonPath("$.list.todos[0].task", equalTo("this and that")));
        assertThat(responseContent, hasJsonPath("$.list.todos[0]._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$.list.todos[0]._links.delete.href", containsString("v1/todos/" + firstTodo.getTodoId().getIdentifier())));
        assertThat(responseContent, hasJsonPath("$.list.todos[0]._links.update.href", containsString("v1/todos/" + firstTodo.getTodoId().getIdentifier())));
        assertThat(responseContent, hasJsonPath("$.list.todos[0]._links.complete.href", containsString("v1/todos/" + firstTodo.getTodoId().getIdentifier() + "/complete")));
        assertThat(responseContent, hasJsonPath("$.list.todos[0]._links.move.href", containsString("v1/todos/" + firstTodo.getTodoId().getIdentifier() + "/move/" + firstTodo.getTodoId().getIdentifier())));
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/lists/" + defaultListId.get())));
    }

    private void doGet() throws Exception {
        mvcResult = mockMvc.perform(mockRequestBuilder)
                .andReturn();
    }
}
