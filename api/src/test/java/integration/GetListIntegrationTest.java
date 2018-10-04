package integration;

import com.doerapispring.domain.ListService;
import com.doerapispring.domain.TodoService;
import com.doerapispring.domain.UniqueIdentifier;
import com.doerapispring.domain.User;
import com.doerapispring.web.MasterListDTO;
import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.TodoDTO;
import com.doerapispring.web.UserSessionsApiService;
import org.hamcrest.Matchers;
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

public class GetListIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private MvcResult mvcResult;
    private final HttpHeaders httpHeaders = new HttpHeaders();
    private MockHttpServletRequestBuilder baseMockRequestBuilder;
    private MockHttpServletRequestBuilder mockRequestBuilder;

    @Autowired
    private UserSessionsApiService userSessionsApiService;

    @Autowired
    private TodoService todoService;
    @Autowired
    private ListService listService;
    private User user;

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
    public void list() throws Exception {
        mockRequestBuilder = baseMockRequestBuilder;
        listService.unlock(user);
        todoService.create(user, "this and that");
        todoService.createDeferred(user, "here and there");
        MasterListDTO masterList = listService.get(user);
        TodoDTO todo = masterList.getTodos().get(0);
        TodoDTO deferredTodo = masterList.getDeferredTodos().get(0);

        doGet();

        String responseContent = mvcResult.getResponse().getContentAsString();

        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$.list", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$.list.name", equalTo("now")));
        assertThat(responseContent, hasJsonPath("$.list.deferredName", equalTo("later")));
        assertThat(responseContent, hasJsonPath("$.list.unlockDuration", not(Matchers.isEmptyString())));
        assertThat(responseContent, hasJsonPath("$.list.todos", hasSize(1)));
        assertThat(responseContent, hasJsonPath("$.list.todos[0].task", equalTo("this and that")));
        assertThat(responseContent, hasJsonPath("$.list.todos[0]._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$.list.todos[0]._links.delete.href", containsString("v1/todos/" + todo.getIdentifier())));
        assertThat(responseContent, hasJsonPath("$.list.todos[0]._links.update.href", containsString("v1/todos/" + todo.getIdentifier())));
        assertThat(responseContent, hasJsonPath("$.list.todos[0]._links.complete.href", containsString("v1/todos/" + todo.getIdentifier() + "/complete")));
        assertThat(responseContent, hasJsonPath("$.list.todos[0]._links.move.href", containsString("v1/todos/" + todo.getIdentifier() + "/move/" + todo.getIdentifier())));
        assertThat(responseContent, hasJsonPath("$.list.deferredTodos", hasSize(1)));
        assertThat(responseContent, hasJsonPath("$.list.deferredTodos[0].task", equalTo("here and there")));
        assertThat(responseContent, hasJsonPath("$.list.deferredTodos[0]._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$.list.deferredTodos[0]._links.delete.href", containsString("v1/todos/" + deferredTodo.getIdentifier())));
        assertThat(responseContent, hasJsonPath("$.list.deferredTodos[0]._links.update.href", containsString("v1/todos/" + deferredTodo.getIdentifier())));
        assertThat(responseContent, hasJsonPath("$.list.deferredTodos[0]._links.complete.href", containsString("v1/todos/" + deferredTodo.getIdentifier() + "/complete")));
        assertThat(responseContent, hasJsonPath("$.list.deferredTodos[0]._links.move.href", containsString("v1/todos/" + deferredTodo.getIdentifier() + "/move/" + deferredTodo.getIdentifier())));
        assertThat(responseContent, hasJsonPath("$.list._links", not(Matchers.isEmptyString())));
        assertThat(responseContent, hasJsonPath("$.list._links.create.href", containsString("/v1/list/todos")));
        assertThat(responseContent, hasJsonPath("$.list._links.createDeferred.href", containsString("/v1/list/deferredTodos")));
        assertThat(responseContent, hasJsonPath("$.list._links.pull.href", containsString("/v1/list/pull")));
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/list")));
    }

    private void doGet() throws Exception {
        mvcResult = mockMvc.perform(mockRequestBuilder)
                .andReturn();
    }
}
