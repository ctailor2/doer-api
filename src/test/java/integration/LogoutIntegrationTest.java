package integration;

import com.doerapispring.apiTokens.SessionToken;
import com.doerapispring.apiTokens.SessionTokenService;
import com.doerapispring.userSessions.UserSessionsService;
import com.doerapispring.users.User;
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
        User user = User.builder()
                .email("email")
                .password("password")
                .build();
        user = userSessionsService.signup(user);
        httpHeaders.add("Session-Token", user.getSessionToken().getToken());

        doPost();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        SessionToken sessionToken = sessionTokenService.getActive(user.getEmail());
        assertThat(sessionToken.getExpiresAt()).isToday();
    }
}
