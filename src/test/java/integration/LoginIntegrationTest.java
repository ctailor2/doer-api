package integration;

import com.doerapispring.authentication.UserSessionsService;
import com.doerapispring.web.SessionTokenDTO;
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
        // TODO: Refactor this to use json path matchers
        userSessionsService.signup("test@email.com", "password");

        doPost();

        MockHttpServletResponse response = mvcResult.getResponse();
        String contentAsString = response.getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        SessionTokenDTO sessionToken = mapper.readValue(contentAsString, SessionTokenDTO.class);

        assertThat(sessionToken.getToken()).isNotEmpty();
        assertThat(sessionToken.getExpiresAt()).isInTheFuture();
    }
}
