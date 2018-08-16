package integration;

import com.doerapispring.domain.ListService;
import com.doerapispring.domain.TodoService;
import com.doerapispring.domain.UniqueIdentifier;
import com.doerapispring.domain.User;
import com.doerapispring.web.MasterListDTO;
import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import org.assertj.core.api.Assertions;
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

    private final HttpHeaders httpHeaders = new HttpHeaders();

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
    }

    @Test
    public void pull() throws Exception {
        todosService.createDeferred(user, "will get pulled");
        todosService.createDeferred(user, "will also get pulled");
        todosService.createDeferred(user, "keep for later");

        MvcResult mvcResult = mockMvc.perform(post("/v1/list/pull")
            .headers(httpHeaders))
            .andReturn();
        String responseContent = mvcResult.getResponse().getContentAsString();
        User user = new User(new UniqueIdentifier<>("test@email.com"));
        listService.unlock(user);
        MasterListDTO newMasterList = listService.get(user);

        Assertions.assertThat(newMasterList.getTodos()).extracting("task")
            .containsExactly("will get pulled", "will also get pulled");
        Assertions.assertThat(newMasterList.getDeferredTodos()).extracting("task")
            .containsExactly("keep for later");
        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/list/pull")));
        assertThat(responseContent, hasJsonPath("$._links.list.href", containsString("/v1/list")));
    }
}
