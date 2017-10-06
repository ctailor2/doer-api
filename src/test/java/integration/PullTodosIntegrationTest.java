package integration;

import com.doerapispring.domain.MasterList;
import com.doerapispring.domain.TodoService;
import com.doerapispring.domain.UniqueIdentifier;
import com.doerapispring.domain.User;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class PullTodosIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private User user;
    private MvcResult mvcResult;

    private HttpHeaders httpHeaders = new HttpHeaders();

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
    }

    @Test
    public void pull() throws Exception {
        todosService.createDeferred(user, "will get pulled");
        todosService.createDeferred(user, "will also get pulled");
        todosService.createDeferred(user, "keep for later");

        mvcResult = mockMvc.perform(post("/v1/list/pull")
                .headers(httpHeaders))
                .andReturn();
        String responseContent = mvcResult.getResponse().getContentAsString();
        MasterList newMasterList = todosService.get(new User(new UniqueIdentifier<>("test@email.com")));

        assertThat(newMasterList.getAllTodos(), hasItem(allOf(
                hasProperty("task", equalTo("will get pulled")),
                hasProperty("listName", equalTo(MasterList.NAME)),
                hasProperty("position", equalTo(1)))));
        assertThat(newMasterList.getAllTodos(), hasItem(allOf(
                hasProperty("task", equalTo("will also get pulled")),
                hasProperty("listName", equalTo(MasterList.NAME)),
                hasProperty("position", equalTo(2)))));
        assertThat(newMasterList.getAllTodos(), hasItem(allOf(
                hasProperty("task", equalTo("keep for later")),
                hasProperty("listName", equalTo(MasterList.DEFERRED_NAME)),
                hasProperty("position", equalTo(3)))));
        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/list/pull")));
        assertThat(responseContent, hasJsonPath("$._links.list.href", containsString("/v1/list")));
    }
}
