package integration;

import com.doerapispring.authentication.Credentials;
import com.doerapispring.authentication.SessionToken;
import com.doerapispring.authentication.UserSessionsService;
import com.doerapispring.domain.UserIdentifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class LoginIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {

    private MvcResult mvcResult;
    private final String content =
            "{\n" +
                    "  \"identifier\": \"test@email.com\",\n" +
                    "  \"credentials\": \"password\"\n" +
                    "}";

    @Autowired
    @SuppressWarnings("unused")
    private UserSessionsService userSessionsService;

    private void doPost() throws Exception {
        mvcResult = mockMvc.perform(post("/v1/login")
                .content(content)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    @Test
    public void login_whenUserWithEmailRegistered_correctPassword_respondsWithSessionToken() throws Exception {
        userSessionsService.signup(new UserIdentifier("test@email.com"),
                new Credentials("password"));

        doPost();

        MockHttpServletResponse response = mvcResult.getResponse();
        String contentAsString = response.getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        SessionToken sessionToken = mapper.readValue(contentAsString, SessionToken.class);

        assertThat(sessionToken.getToken()).isNotEmpty();
        assertThat(sessionToken.getExpiresAt()).isInTheFuture();
    }
}
