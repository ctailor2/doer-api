package integration;

import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class RootResourcesIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {

    private final HttpHeaders httpHeaders = new HttpHeaders();

    @Autowired
    private UserSessionsApiService userSessionsApiService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        String identifier = "test@email.com";
        SessionTokenDTO signupSessionToken = userSessionsApiService.signup(identifier, "password");
        httpHeaders.add("Session-Token", signupSessionToken.getToken());
    }

    @Test
    public void rootResources() throws Exception {
        mockMvc.perform(get("/v1/resources/root").headers(httpHeaders))
                .andExpect(jsonPath("$._links", not(isEmptyString())))
                .andExpect(jsonPath("$._links.self.href", containsString("/v1/resources/root")))
                .andExpect(jsonPath("$._links.listResources.href", containsString("/v1/resources/list")))
                .andExpect(jsonPath("$._links.historyResources.href", containsString("/v1/resources/history")));
    }

}
