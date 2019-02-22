package integration;

import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class BaseResourcesIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {

    @Test
    public void baseResources_includesLinkToGetTodos() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/v1/resources/base")).andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();

        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1")));
        assertThat(responseContent, hasJsonPath("$._links.login.href", containsString("/v1/login")));
        assertThat(responseContent, hasJsonPath("$._links.signup.href", containsString("/v1/signup")));
    }

    @Test
    public void baseResources_isAvailableFromTheRoot() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/v1")).andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();

        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString("/v1")));
        assertThat(responseContent, hasJsonPath("$._links.login.href", containsString("/v1/login")));
        assertThat(responseContent, hasJsonPath("$._links.signup.href", containsString("/v1/signup")));
    }
}
