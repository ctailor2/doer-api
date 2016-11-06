package integration;

import com.doerapispring.Credentials;
import com.doerapispring.UserIdentifier;
import com.doerapispring.apiTokens.SessionToken;
import com.doerapispring.apiTokens.SessionTokenService;
import com.doerapispring.userSessions.UserSessionsService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import javax.servlet.http.HttpServletResponse;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Created by chiragtailor on 9/21/16.
 */
public class LogoutIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private MvcResult mvcResult;

    private HttpHeaders httpHeaders = new HttpHeaders();

    @Autowired
    UserSessionsService userSessionsService;

    @Autowired
    SessionTokenService sessionTokenService;

    private void doPost() throws Exception {
        mvcResult = mockMvc.perform(post("/v1/logout")
                .headers(httpHeaders))
                .andReturn();
    }

    @Test
    public void logout_whenSessionTokenExists_expiresToken_respondsWithOk() throws Exception {
        SessionToken signupSessionToken = userSessionsService.signup(new UserIdentifier("email"), new Credentials("password"));
        httpHeaders.add("Session-Token", signupSessionToken.getToken());

        doPost();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        SessionToken sessionToken = sessionTokenService.getActive("email");
        assertThat(sessionToken.getExpiresAt()).isToday();
    }
}
