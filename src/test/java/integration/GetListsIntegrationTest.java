package integration;

import com.doerapispring.domain.UniqueIdentifier;
import com.doerapispring.domain.User;
import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.text.IsEmptyString.isEmptyString;

public class GetListsIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private MvcResult mvcResult;
    private HttpHeaders httpHeaders = new HttpHeaders();
    private MockHttpServletRequestBuilder baseMockRequestBuilder;
    private MockHttpServletRequestBuilder mockRequestBuilder;
    private User user;

    @Autowired
    private UserSessionsApiService userSessionsApiService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        String identifier = "test@email.com";
        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier<>(identifier);
        user = new User(uniqueIdentifier);
        SessionTokenDTO signupSessionToken = userSessionsApiService.signup(identifier, "password");
        httpHeaders.add("Session-Token", signupSessionToken.getToken());
        baseMockRequestBuilder = MockMvcRequestBuilders
                .get("/v1/lists")
                .headers(httpHeaders);
    }

    @Test
    public void todos_whenUserHasLaterTodos_returnsLaterTodos() throws Exception {
        mockRequestBuilder = baseMockRequestBuilder;

        doGet();

        String responseContent = mvcResult.getResponse().getContentAsString();

        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$.lists", hasSize(2)));
        assertThat(responseContent, hasJsonPath("$.lists[0].name", equalTo("now")));
        assertThat(responseContent, hasJsonPath("$.lists[0]._links", not(Matchers.isEmptyString())));
        assertThat(responseContent, hasJsonPath("$.lists[0]._links.list.href", containsString("v1/lists/now")));
        assertThat(responseContent, hasJsonPath("$.lists[1].name", equalTo("later")));
        assertThat(responseContent, hasJsonPath("$.lists[1]._links", not(Matchers.isEmptyString())));
        assertThat(responseContent, hasJsonPath("$.lists[1]._links.list.href", containsString("v1/lists/later")));
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1/lists")));
    }

    private void doGet() throws Exception {
        mvcResult = mockMvc.perform(mockRequestBuilder)
                .andReturn();
    }
}
