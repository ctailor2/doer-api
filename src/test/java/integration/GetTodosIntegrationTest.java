package integration;

import com.doerapispring.Credentials;
import com.doerapispring.UserIdentifier;
import com.doerapispring.apiTokens.SessionToken;
import com.doerapispring.todos.ScheduledFor;
import com.doerapispring.todos.TodoService;
import com.doerapispring.userSessions.UserSessionsService;
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
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created by chiragtailor on 9/27/16.
 */
public class GetTodosIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private MvcResult mvcResult;

    private HttpHeaders httpHeaders = new HttpHeaders();

    @Autowired
    UserSessionsService userSessionsService;

    @Autowired
    TodoService todosService;


    private MockHttpServletRequestBuilder baseMockRequestBuilder;
    private MockHttpServletRequestBuilder mockRequestBuilder;
    private UserIdentifier userIdentifier;

    private void doGet() throws Exception {
        mvcResult = mockMvc.perform(mockRequestBuilder)
                .andReturn();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        userIdentifier = new UserIdentifier("test@email.com");
        SessionToken signupSessionToken = userSessionsService.signup(
                userIdentifier,
                new Credentials("password"));
        httpHeaders.add("Session-Token", signupSessionToken.getToken());
        baseMockRequestBuilder = MockMvcRequestBuilders
                .get("/v1/todos")
                .headers(httpHeaders);
    }

    @Test
    public void todos_whenUserHasTodos_returnsAllTodos() throws Exception {
        mockRequestBuilder = baseMockRequestBuilder;
        todosService.create(new UserIdentifier("test@email.com"), "this and that", ScheduledFor.later);

        doGet();

        String responseContent = mvcResult.getResponse().getContentAsString();

        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$", hasSize(equalTo(1))));
        assertThat(responseContent, hasJsonPath("$[0].task", equalTo("this and that")));
        assertThat(responseContent, hasJsonPath("$[0].scheduling", equalTo("later")));
    }

    @Test
    public void todos_whenUserHasTodos_withQueryForActive_returnsActiveTodos() throws Exception {
        mockRequestBuilder = baseMockRequestBuilder.param("scheduling", "now");
        todosService.create(userIdentifier, "now task", ScheduledFor.now);
        todosService.create(userIdentifier, "later task", ScheduledFor.later);

        doGet();

        String responseContent = mvcResult.getResponse().getContentAsString();

        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$", hasSize(equalTo(1))));
        assertThat(responseContent, hasJsonPath("$[0].task", equalTo("now task")));
        assertThat(responseContent, hasJsonPath("$[0].scheduling", equalTo("now")));
    }
}
