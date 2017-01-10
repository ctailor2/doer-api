package integration;

import com.doerapispring.authentication.UserSessionsService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class LoginIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {

    private MvcResult mvcResult;
    private final String content =
            "{\n" +
                    "  \"email\": \"test@email.com\",\n" +
                    "  \"password\": \"password\"\n" +
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
        userSessionsService.signup("test@email.com", "password");

        doPost();

        String responseContent = mvcResult.getResponse().getContentAsString();

        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$.token", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$.expiresAt", not(isEmptyString())));
    }
}
