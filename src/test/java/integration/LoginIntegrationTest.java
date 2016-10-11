package integration;

import com.doerapispring.userSessions.UserSessionsService;
import com.doerapispring.users.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Created by chiragtailor on 8/28/16.
 */
public class LoginIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {

    private MvcResult mvcResult;
    private final String content =
            "{\n" +
                    "  \"email\": \"test@email.com\",\n" +
                    "  \"password\": \"password\"\n" +
                    "}";

    @Autowired
    private UserSessionsService userSessionsService;

    private void doPost() throws Exception {
        mvcResult = mockMvc.perform(post("/v1/login")
                .content(content)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    @Test
    public void login_whenUserWithEmailRegistered_correctPassword_respondsWithUserEntity_withLoginFields_respondsWithSessionTokenEntity_withSessionTokenFields() throws Exception {
        User registeredUser = User.builder()
                .email("test@email.com")
                .password("password")
                .build();
        userSessionsService.signup(registeredUser);

        doPost();

        MockHttpServletResponse response = mvcResult.getResponse();
        String contentAsString = response.getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        User user = mapper.readValue(contentAsString, User.class);

        assertThat(user.getEmail()).isEqualTo("test@email.com");
        assertThat(user.getSessionToken().getToken()).isNotEmpty();
        assertThat(user.getSessionToken().getExpiresAt()).isInTheFuture();
    }
}
