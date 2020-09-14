package integration;

import com.doerapispring.web.UserSessionsApiService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class LoginIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {

    @Autowired
    @SuppressWarnings("unused")
    private UserSessionsApiService userSessionsApiService;

    @Test
    public void login() throws Exception {
        userSessionsApiService.signup("test@email.com", "password");

        String content = "{\n" +
            "  \"email\": \"test@email.com\",\n" +
            "  \"password\": \"password\"\n" +
            "}";
        mockMvc.perform(post("/v1/login")
                .content(content)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.session", not(isEmptyString())))
                .andExpect(jsonPath("$.session.token", not(isEmptyString())))
                .andExpect(jsonPath("$.session.expiresAt", not(isEmptyString())))
                .andExpect(jsonPath("$._links", not(isEmptyString())))
                .andExpect(jsonPath("$._links.self.href", containsString("/v1/login")))
                .andExpect(jsonPath("$._links.root.href", containsString("/v1/resources/root")));
    }
}
