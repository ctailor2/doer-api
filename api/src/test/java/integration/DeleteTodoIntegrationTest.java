package integration;

import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import com.jayway.jsonpath.JsonPath;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DeleteTodoIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {

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
    public void delete_removesTodo() throws Exception {
        String nextActionHref = JsonPath.parse(mockMvc.perform(get("/v1/lists/default")
                .headers(httpHeaders))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).read("$.list._links.create.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(post(nextActionHref)
                .content("{\"task\":\"some task\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString()).read("$.list.todos[0]._links.delete.href", String.class);

        mockMvc.perform(delete(nextActionHref)
                .headers(httpHeaders))
                .andExpect(jsonPath("$.list.todos", hasSize(0)));
    }
}
