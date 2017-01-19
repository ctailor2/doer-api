package integration;

import com.doerapispring.domain.*;
import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class DisplaceTodoIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    @Autowired
    UserSessionsApiService userSessionsApiService;

    @Autowired
    TodoService todoService;

    private User user;
    private HttpHeaders httpHeaders = new HttpHeaders();
    private MvcResult mvcResult;

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
    public void displace() throws Exception {
        ScheduledFor scheduling = ScheduledFor.now;
        todoService.create(user, "someTask", scheduling);
        List<Todo> todos = todoService.getByScheduling(user, scheduling);
        String localIdentifier = todos.get(0).getLocalIdentifier();

        doPost(localIdentifier);

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(todoService.getByScheduling(user, ScheduledFor.now).size()).isEqualTo(1);
        assertThat(todoService.getByScheduling(user, ScheduledFor.now).get(0))
                .isEqualTo(new Todo("1i", "aMoreImportantTask", ScheduledFor.now));
        assertThat(todoService.getByScheduling(user, ScheduledFor.later).size()).isEqualTo(1);
        assertThat(todoService.getByScheduling(user, ScheduledFor.later).get(0))
                .isEqualTo(new Todo("1", "someTask", ScheduledFor.later));
    }

    private void doPost(String localIdentifier) throws Exception {
        mvcResult = mockMvc.perform(post("v1/todos/" + localIdentifier + "/displace")
                .content("{\"task\": \"aMoreImportantTask\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
                .andReturn();
    }
}
