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
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.text.IsEmptyString.isEmptyString;

public class GetLaterTodosIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
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
                .get("/v1/list/deferredTodos")
                .headers(httpHeaders);
    }

    @Test
    public void deferredTodos() throws Exception {
        mockRequestBuilder = baseMockRequestBuilder;
        todosService.create(user, "this and that");
        todosService.createDeferred(user, "here and now");
        todosService.createDeferred(user, "near and far");
        listService.unlock(user);
        MasterList masterList = todosService.get(user);
        Todo secondTodo = masterList.getDeferredTodos().get(0);
        Todo thirdTodo = masterList.getDeferredTodos().get(1);

        doGet();

        String responseContent = mvcResult.getResponse().getContentAsString();

        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$.todos", hasSize(2)));
        assertThat(responseContent, hasJsonPath("$.todos[0].task", equalTo("here and now")));
        assertThat(responseContent, hasJsonPath("$.todos[0]._links.move", hasSize(2)));
        assertThat(responseContent, hasJsonPath("$.todos[0]._links.move[0].href", containsString("v1/todos/" + secondTodo.getLocalIdentifier() + "/move/" + secondTodo.getLocalIdentifier())));
        assertThat(responseContent, hasJsonPath("$.todos[0]._links.move[1].href", containsString("v1/todos/" + secondTodo.getLocalIdentifier() + "/move/" + thirdTodo.getLocalIdentifier())));
        assertThat(responseContent, hasJsonPath("$.todos[1].task", equalTo("near and far")));
        assertThat(responseContent, hasJsonPath("$.todos[1]._links.move", hasSize(2)));
        assertThat(responseContent, hasJsonPath("$.todos[1]._links.move[0].href", containsString("v1/todos/" + thirdTodo.getLocalIdentifier() + "/move/" + secondTodo.getLocalIdentifier())));
        assertThat(responseContent, hasJsonPath("$.todos[1]._links.move[1].href", containsString("v1/todos/" + thirdTodo.getLocalIdentifier() + "/move/" + thirdTodo.getLocalIdentifier())));
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", endsWith("/v1/list/deferredTodos")));
        assertThat(responseContent, hasJsonPath("$._links.list.href", endsWith("/v1/list")));
    }

    private void doGet() throws Exception {
        mvcResult = mockMvc.perform(mockRequestBuilder).andReturn();
    }
}
