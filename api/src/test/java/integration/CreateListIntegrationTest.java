package integration;

import com.doerapispring.domain.ListApplicationService;
import com.doerapispring.domain.TodoApplicationService;
import com.doerapispring.domain.User;
import com.doerapispring.domain.UserService;
import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;

public class CreateListIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private final HttpHeaders httpHeaders = new HttpHeaders();
    private MockHttpServletRequestBuilder baseMockRequestBuilder;

    @Autowired
    private UserSessionsApiService userSessionsApiService;

    @Autowired
    private TodoApplicationService todoApplicationService;

    @Autowired
    private ListApplicationService listApplicationService;

    @Autowired
    private UserService userService;

    private User user;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        String identifier = "test@email.com";
        SessionTokenDTO signupSessionToken = userSessionsApiService.signup(identifier, "password");
        user = userService.find(identifier).orElseThrow(RuntimeException::new);
        httpHeaders.add("Session-Token", signupSessionToken.getToken());
        baseMockRequestBuilder = MockMvcRequestBuilders
            .post("/v1/lists")
            .headers(httpHeaders);
    }

    @Test
    public void list() throws Exception {
        String listName = "someName";
        MockHttpServletRequestBuilder mockRequestBuilder = baseMockRequestBuilder
            .content("{\n  \"name\": \"" + listName + "\"}")
            .contentType(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = mockMvc.perform(mockRequestBuilder)
            .andReturn();

        Assertions.assertThat(listApplicationService.getAll(user)).hasSize(2);

        String responseContent = mvcResult.getResponse().getContentAsString();
        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$._links.self.href", endsWith("/v1/lists")));
    }
}
