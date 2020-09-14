package integration;

import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import com.jayway.jsonpath.JsonPath;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class CreateListIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
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
    public void list() throws Exception {
        String listName = "someName";
        String nextActionHref = JsonPath.parse(mockMvc.perform(post("/v1/lists")
                .headers(httpHeaders)
                .content("{\n  \"name\": \"" + listName + "\"}")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString()).read("$._links.lists.href", String.class);

        mockMvc.perform(get(nextActionHref)
                .headers(httpHeaders))
                .andExpect(jsonPath("$.lists", hasSize(2)))
                .andExpect(jsonPath("$.lists[0].name", equalTo("default")))
                .andExpect(jsonPath("$.lists[1].name", equalTo(listName)));
    }
}
