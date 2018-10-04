package integration;

import com.doerapispring.domain.ListService;
import com.doerapispring.domain.TodoService;
import com.doerapispring.domain.UniqueIdentifier;
import com.doerapispring.domain.User;
import com.doerapispring.web.MasterListDTO;
import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.TodoDTO;
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
import static org.hamcrest.Matchers.*;
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
    private TodoService todosService;

    @Autowired
    private ListService listService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        String identifier = "test@email.com";
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>(identifier);
        user = new User(uniqueIdentifier);
        SessionTokenDTO signupSessionToken = userSessionsApiService.signup(identifier, "password");
        httpHeaders.add("Session-Token", signupSessionToken.getToken());
        baseMockRequestBuilder = MockMvcRequestBuilders
                .get("/v1/list")
                .headers(httpHeaders);
    }

    @Test
    public void todos() throws Exception {
        mockRequestBuilder = baseMockRequestBuilder;
        todosService.create(user, "this and that");
        MasterListDTO masterList = listService.get(user);
        TodoDTO firstTodo = masterList.getTodos().get(0);

        doGet();

        String responseContent = mvcResult.getResponse().getContentAsString();

        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$.list.todos", hasSize(1)));
        assertThat(responseContent, hasJsonPath("$.list.todos[0].task", equalTo("this and that")));
        assertThat(responseContent, hasJsonPath("$.list.todos[0]._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$.list.todos[0]._links.delete.href", containsString("v1/todos/" + firstTodo.getIdentifier())));
        assertThat(responseContent, hasJsonPath("$.list.todos[0]._links.update.href", containsString("v1/todos/" + firstTodo.getIdentifier())));
        assertThat(responseContent, hasJsonPath("$.list.todos[0]._links.complete.href", containsString("v1/todos/" + firstTodo.getIdentifier() + "/complete")));
        assertThat(responseContent, hasJsonPath("$.list.todos[0]._links.move.href", containsString("v1/todos/" + firstTodo.getIdentifier() + "/move/" + firstTodo.getIdentifier())));
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", endsWith("/v1/list")));
    }

    private void doGet() throws Exception {
        mvcResult = mockMvc.perform(mockRequestBuilder)
                .andReturn();
    }
}
