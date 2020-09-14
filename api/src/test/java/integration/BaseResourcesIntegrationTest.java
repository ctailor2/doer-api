package integration;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class BaseResourcesIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    @Test
    public void baseResources_isAvailableFromTheRoot() throws Exception {
        mockMvc.perform(get("/v1/"))
                .andExpect(jsonPath("$._links", not(isEmptyString())))
                .andExpect(jsonPath("$._links.self.href", containsString("/v1/")))
                .andExpect(jsonPath("$._links.login.href", containsString("/v1/login")))
                .andExpect(jsonPath("$._links.signup.href", containsString("/v1/signup")));
    }
}
