package integration;

import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class TodoResourcesIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private MvcResult mvcResult;
    private HttpHeaders httpHeaders = new HttpHeaders();

    @Autowired
    @SuppressWarnings("unused")
    private UserSessionsApiService userSessionsApiService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        String identifier = "test@email.com";
        SessionTokenDTO signupSessionToken = userSessionsApiService.signup(identifier, "password");
        httpHeaders.add("Session-Token", signupSessionToken.getToken());
    }

    @Test
    public void todoResources_includesLinks() throws Exception {
        doGet();

        String responseContent = mvcResult.getResponse().getContentAsString();

        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/resources/todo")));
        assertThat(responseContent, hasJsonPath("$._links.todoNow.href", containsString("/v1/todoNow")));
        assertThat(responseContent, hasJsonPath("$._links.todoLater.href", containsString("/v1/todoLater")));
        assertThat(responseContent, hasJsonPath("$._links.pull.href", containsString("/v1/todos/pull")));
        assertThat(responseContent, hasJsonPath("$._links.nowTodos.href", containsString("/v1/todos")));
        assertThat(responseContent, hasJsonPath("$._links.laterTodos.href", containsString("/v1/todos")));
    }

    private void doGet() throws Exception {
        mvcResult = mockMvc.perform(get("/v1/resources/todo").headers(httpHeaders)).andReturn();
    }
}
