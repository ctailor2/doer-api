package integration;

import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import com.jayway.jsonpath.JsonPath;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class HistoryResourcesIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
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
    public void historyResources() throws Exception {
        String defaultListHref = JsonPath.parse(mockMvc.perform(get("/v1/lists/default")
                .headers(httpHeaders))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).read("$._links.self.href", String.class);

        mockMvc.perform(get("/v1/resources/history").headers(httpHeaders))
                .andExpect(jsonPath("$._links", not(isEmptyString())))
                .andExpect(jsonPath("$._links.self.href", containsString("/v1/resources/history")))
                .andExpect(jsonPath("$._links.completedList.href", equalTo(defaultListHref + "/completed")));
    }
}
