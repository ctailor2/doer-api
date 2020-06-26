package integration;

import com.doerapispring.domain.ListApplicationService;
import com.doerapispring.domain.ListId;
import com.doerapispring.domain.User;
import com.doerapispring.domain.UserService;
import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DefaultListIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private final HttpHeaders httpHeaders = new HttpHeaders();

    @Autowired
    private UserSessionsApiService userSessionsApiService;

    @Autowired
    private ListApplicationService listApplicationService;

    @Autowired
    private UserService userService;

    private User user;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        String identifier = "test@email.com";
        SessionTokenDTO signupSessionToken = userSessionsApiService.signup(identifier, "password");
        httpHeaders.add("Session-Token", signupSessionToken.getToken());
        user = userService.find(identifier).orElseThrow(RuntimeException::new);
    }

    @Test
    public void list() throws Exception {
        mockMvc.perform(get("/v1/lists/default")
            .headers(httpHeaders))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.list.profileName", equalTo("default")));

        String otherListName = "someListName";
        listApplicationService.create(user, otherListName);
        ListId otherListId = listApplicationService.getAll(user).get(1).getListId();

        mockMvc.perform(post("/v1/lists/" + otherListId.get() + "/default")
            .headers(httpHeaders))
            .andExpect(status().isAccepted());

        mockMvc.perform(get("/v1/lists/default")
            .headers(httpHeaders))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.list.profileName", equalTo(otherListName)))
            .andExpect(jsonPath("$._links", not(isEmptyString())))
            .andExpect(jsonPath("$._links.self.href", containsString("/v1/lists/" + otherListId.get())));
    }
}
