package integration;

import com.doerapispring.apiTokens.SessionToken;
import com.doerapispring.apiTokens.SessionTokenService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Created by chiragtailor on 9/5/16.
 */
public class PingAuthenticatedIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private MvcResult mvcResult;

    private HttpHeaders httpHeaders = new HttpHeaders();

    @Autowired
    private SessionTokenService sessionTokenService;

    private void doGet() throws Exception {
        mvcResult = mockMvc.perform(get("/v1/pingAuthenticated")
                .headers(httpHeaders))
                .andReturn();
    }

    @Test
    public void pingAuthenticated_withNoSessionTokenHeader_respondsWithBadRequest() throws Exception {
        doGet();

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void pingAuthenticated_withSessionTokenHeader_thatDoesNotExist_respondsWithUnauthorized() throws Exception {
        httpHeaders.add("Session-Token", "token");

        doGet();

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void pingAuthenticated_withSessionTokenHeader_thatExists_respondsWithOk() throws Exception {
        SessionToken sessionToken = sessionTokenService.create(1L);
        httpHeaders.add("Session-Token", sessionToken.token);

        doGet();

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }
}
