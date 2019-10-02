package integration;

import com.doerapispring.domain.*;
import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import com.jayway.jsonpath.JsonPath;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.text.IsEmptyString.isEmptyString;

public class GetCompletedListIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
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

    private User user;
    private ListId defaultListId;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        String identifier = "test@email.com";
        user = new User(new UserId(identifier));
        SessionTokenDTO signupSessionToken = userSessionsApiService.signup(identifier, "password");
        defaultListId = listApplicationService.getDefault(user).getListId();
        todoApplicationService.createDeferred(user, defaultListId, "someTask");
        httpHeaders.add("Session-Token", signupSessionToken.getToken());
        baseMockRequestBuilder = MockMvcRequestBuilders
            .get("/v1/lists/" + defaultListId.get() + "/completed")
            .headers(httpHeaders);
    }

    @Test
    public void list() throws Exception {
        mockRequestBuilder = baseMockRequestBuilder;
        todoApplicationService.create(user, defaultListId, "some task");
        ReadOnlyTodoList todoList = listApplicationService.getDefault(user);
        Todo todo = todoList.getTodos().get(0);
        todoApplicationService.complete(user, defaultListId, new TodoId(todo.getTodoId().getIdentifier()));

        doGet();

        String responseContent = mvcResult.getResponse().getContentAsString();

        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$.list", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$.list.todos", hasSize(1)));
        assertThat(responseContent, hasJsonPath("$.list.todos[0].task", equalTo("some task")));
        String completedAtString = JsonPath.parse(responseContent).read("$.list.todos[0].completedAt", String.class);
        Assertions.assertThat(completedAtString).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\+\\d{4}");
        Date completedAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(completedAtString);
        Assertions.assertThat(completedAt).isToday();
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/lists/" + defaultListId.get() + "/completed")));
    }

    private void doGet() throws Exception {
        mvcResult = mockMvc.perform(mockRequestBuilder)
            .andReturn();
    }
}
