package integration;

import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import com.jayway.jsonpath.JsonPath;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.text.IsEmptyString.isEmptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetCompletedListIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
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
        String nextActionHref = JsonPath.parse(mockMvc.perform(get("/v1/lists/default")
                .headers(httpHeaders))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).read("$.list._links.create.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(post(nextActionHref)
                .content("{\"task\":\"default task\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString()).read("$.list.todos[0]._links.complete.href");
        mockMvc.perform(post(nextActionHref)
                .headers(httpHeaders));

        nextActionHref = JsonPath.parse(mockMvc.perform(post("/v1/lists")
                .headers(httpHeaders)
                .content("{\n  \"name\": \"someOtherList\"}")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString()).read("$._links.lists.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(get(nextActionHref)
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString()).read("$.lists[1]._links.list.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(get(nextActionHref)
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString()).read("$.list._links.create.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(post(nextActionHref)
                .content("{\"task\":\"someOtherList task\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString()).read("$.list.todos[0]._links.complete.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(post(nextActionHref)
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString()).read("$.list._links.completed.href", String.class);

        MvcResult mvcResult = mockMvc.perform(get(nextActionHref)
                .headers(httpHeaders))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();

        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$.list", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$.list.todos", hasSize(1)));
        assertThat(responseContent, hasJsonPath("$.list.todos[0].task", equalTo("someOtherList task")));
        String completedAtString = JsonPath.parse(responseContent).read("$.list.todos[0].completedAt", String.class);
        Assertions.assertThat(completedAtString).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\+\\d{4}");
        Date completedAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(completedAtString);
        Assertions.assertThat(completedAt).isToday();
    }
}
