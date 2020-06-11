package integration;

import com.doerapispring.domain.*;
import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Clock;
import java.util.Date;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static scala.jdk.javaapi.CollectionConverters.asJava;

public class DisplaceTodoIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private User user;

    private final HttpHeaders httpHeaders = new HttpHeaders();

    @Autowired
    private UserSessionsApiService userSessionsApiService;

    @Autowired
    private TodoApplicationService todoApplicationService;

    @Autowired
    private ListApplicationService listApplicationService;

    @Autowired
    private UserService userService;

    private ListId defaultListId;

    @Autowired
    private Clock clock;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        String identifier = "test@email.com";
        SessionTokenDTO signupSessionToken = userSessionsApiService.signup(identifier, "password");
        user = userService.find(identifier).orElseThrow(RuntimeException::new);
        defaultListId = user.getDefaultListId();
        httpHeaders.add("Session-Token", signupSessionToken.getToken());
    }

    @Test
    public void displace_replacesImmediatelyScheduledTodo_bumpsItToPostponedList() throws Exception {
        todoApplicationService.create(user, defaultListId, "some other task");
        todoApplicationService.create(user, defaultListId, "some task");

        MvcResult mvcResult = mockMvc.perform(post("/v1/lists/" + defaultListId.get() + "/displace")
            .content("{\"task\":\"do the things\"}")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(httpHeaders))
            .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        listApplicationService.unlock(user, defaultListId);
        TodoListModel newTodoList = listApplicationService.get(user, defaultListId);

        Assertions.assertThat(asJava(TodoListModel.getTodos(newTodoList))).extracting("task")
            .containsExactly("do the things", "some task");
        Assertions.assertThat(asJava(TodoListModel.getDeferredTodos(newTodoList, Date.from(clock.instant())))).extracting("task")
            .containsExactly("some other task");
        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/lists/" + defaultListId.get() + "/displace")));
        assertThat(responseContent, hasJsonPath("$._links.list.href", endsWith("/v1/lists/" + defaultListId.get())));
    }
}
