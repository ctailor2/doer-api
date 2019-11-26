package integration;

import com.doerapispring.domain.*;
import com.doerapispring.web.SessionTokenDTO;
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
    private TodoApplicationService todoApplicationService;

    @Autowired
    private ListApplicationService listApplicationService;

    @Autowired
    private UserService userService;

    private User user;

    private ListId defaultListId;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        String identifier = "test@email.com";
        SessionTokenDTO signupSessionToken = userSessionsApiService.signup(identifier, "password");
        user = userService.find(identifier).orElseThrow(RuntimeException::new);
        defaultListId = user.getDefaultListId();
        httpHeaders.add("Session-Token", signupSessionToken.getToken());
        baseMockRequestBuilder = MockMvcRequestBuilders
            .get("/v1/lists/" + defaultListId.get())
            .headers(httpHeaders);
    }

    @Test
    public void list() throws Exception {
        mockRequestBuilder = baseMockRequestBuilder;
        listApplicationService.unlock(user, defaultListId);
        todoApplicationService.create(user, defaultListId, "this and that");
        todoApplicationService.createDeferred(user, defaultListId, "here and there");
        todoApplicationService.createDeferred(user, defaultListId, "near and far");
        TodoListReadModel todoList = listApplicationService.get(user, defaultListId);
        Todo todo = todoList.getTodos().get(0);
        Todo firstDeferredTodo = todoList.getDeferredTodos().get(0);
        Todo secondDeferredTodo = todoList.getDeferredTodos().get(1);

        doGet();

        String responseContent = mvcResult.getResponse().getContentAsString();

        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$.list", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$.list.profileName", equalTo("default")));
        assertThat(responseContent, hasJsonPath("$.list.name", equalTo("now")));
        assertThat(responseContent, hasJsonPath("$.list.deferredName", equalTo("later")));
        assertThat(responseContent, hasJsonPath("$.list.unlockDuration", not(Matchers.isEmptyString())));
        assertThat(responseContent, hasJsonPath("$.list.todos", hasSize(1)));
        assertThat(responseContent, hasJsonPath("$.list.todos[0].task", equalTo("this and that")));
        assertThat(responseContent, hasJsonPath("$.list.todos[0]._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$.list.todos[0]._links.delete.href", containsString("v1/lists/" + defaultListId.get() + "/todos/" + todo.getTodoId().getIdentifier())));
        assertThat(responseContent, hasJsonPath("$.list.todos[0]._links.update.href", containsString("v1/lists/" + defaultListId.get() + "/todos/" + todo.getTodoId().getIdentifier())));
        assertThat(responseContent, hasJsonPath("$.list.todos[0]._links.complete.href", containsString("v1/lists/" + defaultListId.get() + "/todos/" + todo.getTodoId().getIdentifier() + "/complete")));
        assertThat(responseContent, hasJsonPath("$.list.todos[0]._links.move.href", containsString("v1/lists/" + defaultListId.get() + "/todos/" + todo.getTodoId().getIdentifier() + "/move/" + todo.getTodoId().getIdentifier())));
        assertThat(responseContent, hasJsonPath("$.list.deferredTodos", hasSize(2)));
        assertThat(responseContent, hasJsonPath("$.list.deferredTodos[0].task", equalTo("here and there")));
        assertThat(responseContent, hasJsonPath("$.list.deferredTodos[0]._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$.list.deferredTodos[0]._links.delete.href", containsString("v1/lists/" + defaultListId.get() + "/todos/" + firstDeferredTodo.getTodoId().getIdentifier())));
        assertThat(responseContent, hasJsonPath("$.list.deferredTodos[0]._links.update.href", containsString("v1/lists/" + defaultListId.get() + "/todos/" + firstDeferredTodo.getTodoId().getIdentifier())));
        assertThat(responseContent, hasJsonPath("$.list.deferredTodos[0]._links.complete.href", containsString("v1/lists/" + defaultListId.get() + "/todos/" + firstDeferredTodo.getTodoId().getIdentifier() + "/complete")));
        assertThat(responseContent, hasJsonPath("$.list.deferredTodos[0]._links.move[0].href", containsString("v1/lists/" + defaultListId.get() + "/todos/" + firstDeferredTodo.getTodoId().getIdentifier() + "/move/" + firstDeferredTodo.getTodoId().getIdentifier())));
        assertThat(responseContent, hasJsonPath("$.list.deferredTodos[0]._links.move[1].href", containsString("v1/lists/" + defaultListId.get() + "/todos/" + firstDeferredTodo.getTodoId().getIdentifier() + "/move/" + secondDeferredTodo.getTodoId().getIdentifier())));
        assertThat(responseContent, hasJsonPath("$.list.deferredTodos[1].task", equalTo("near and far")));
        assertThat(responseContent, hasJsonPath("$.list.deferredTodos[1]._links.move[0].href", containsString("v1/lists/" + defaultListId.get() + "/todos/" + secondDeferredTodo.getTodoId().getIdentifier() + "/move/" + firstDeferredTodo.getTodoId().getIdentifier())));
        assertThat(responseContent, hasJsonPath("$.list.deferredTodos[1]._links.move[1].href", containsString("v1/lists/" + defaultListId.get() + "/todos/" + secondDeferredTodo.getTodoId().getIdentifier() + "/move/" + secondDeferredTodo.getTodoId().getIdentifier())));
    }

    @Test
    public void defaultListActions() throws Exception {
        mockRequestBuilder = baseMockRequestBuilder;

        doGet();

        String responseContent = mvcResult.getResponse().getContentAsString();

        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$.list._links", not(Matchers.isEmptyString())));
        assertThat(responseContent, hasJsonPath("$.list._links.createDeferred.href", containsString("/v1/lists/" + defaultListId.get() + "/deferredTodos")));
        assertThat(responseContent, hasJsonPath("$.list._links.unlock.href", containsString("/v1/lists/" + defaultListId.get() + "/unlock")));
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/list")));
    }

    @Test
    public void listActions_whenListHasCapacity() throws Exception {
        mockRequestBuilder = baseMockRequestBuilder;
        todoApplicationService.create(user, defaultListId, "this and that");
        todoApplicationService.createDeferred(user, defaultListId, "here and there");

        doGet();

        String responseContent = mvcResult.getResponse().getContentAsString();

        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$.list._links", not(Matchers.isEmptyString())));
        assertThat(responseContent, hasJsonPath("$.list._links.create.href", containsString("/v1/lists/" + defaultListId.get() + "/todos")));
        assertThat(responseContent, hasJsonPath("$.list._links.pull.href", containsString("/v1/lists/" + defaultListId.get() + "/pull")));
    }

    @Test
    public void listActions_whenListDoesNotHaveCapacity() throws Exception {
        mockRequestBuilder = baseMockRequestBuilder;
        todoApplicationService.create(user, defaultListId, "this and that");
        todoApplicationService.create(user, defaultListId, "one and two");
        todoApplicationService.createDeferred(user, defaultListId, "here and there");

        doGet();

        String responseContent = mvcResult.getResponse().getContentAsString();

        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$.list._links", not(Matchers.isEmptyString())));
        assertThat(responseContent, hasJsonPath("$.list._links.displace.href", containsString("/v1/lists/" + defaultListId.get() + "/displace")));
        assertThat(responseContent, hasJsonPath("$.list._links.escalate.href", containsString("/v1/lists/" + defaultListId.get() + "/escalate")));
    }

    private void doGet() throws Exception {
        mvcResult = mockMvc.perform(mockRequestBuilder)
            .andReturn();
    }
}
