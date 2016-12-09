package integration;

import com.doerapispring.Credentials;
import com.doerapispring.UserIdentifier;
import com.doerapispring.apiTokens.SessionToken;
import com.doerapispring.todos.ScheduledFor;
import com.doerapispring.todos.Todo;
import com.doerapispring.todos.TodoService;
import com.doerapispring.userSessions.UserSessionsService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Created by chiragtailor on 10/5/16.
 */
public class CreateTodosIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private MvcResult mvcResult;

    private HttpHeaders httpHeaders = new HttpHeaders();

    @Autowired
    UserSessionsService userSessionsService;

    @Autowired
    TodoService todosService;

    private void doPost() throws Exception {
        mvcResult = mockMvc.perform(post("/v1/todos")
                .content("{\n" +
                        "  \"task\":\"read the things\",\n" +
                        "  \"scheduling\":\"now\"\n" +
                        "}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
                .andReturn();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        SessionToken signupSessionToken = userSessionsService.signup(new UserIdentifier("test@email.com"), new Credentials("password"));
        httpHeaders.add("Session-Token", signupSessionToken.getToken());
    }

    @Test
    public void create() throws Exception {
        doPost();

        List<Todo> todos = todosService.getByScheduling(new UserIdentifier("test@email.com"), ScheduledFor.anytime);

        assertThat(todos.size(), equalTo(1));

        String responseContent = mvcResult.getResponse().getContentAsString();
        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$.task", equalTo("read the things")));
        assertThat(responseContent, hasJsonPath("$.scheduling", equalTo("now")));
    }
}
