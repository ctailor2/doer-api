package integration;

import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.text.IsEmptyString.isEmptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetListIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
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
                .andReturn().getResponse().getContentAsString()).read("$.list._links.unlock.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(post(nextActionHref)
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString()).read("$._links.list.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(get(nextActionHref)
                .headers(httpHeaders))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).read("$.list._links.create.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(post(nextActionHref)
                .content("{\"task\":\"this and that\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString()).read("$.list._links.createDeferred.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(post(nextActionHref)
                .content("{\"task\":\"here and there\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString()).read("$.list._links.createDeferred.href", String.class);
        mockMvc.perform(post(nextActionHref)
                .content("{\"task\":\"near and far\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
                .andExpect(jsonPath("$.list", not(isEmptyString())))
                .andExpect(jsonPath("$.list.name", equalTo("now")))
                .andExpect(jsonPath("$.list.deferredName", equalTo("later")))
                .andExpect(jsonPath("$.list.unlockDuration", not(Matchers.isEmptyString())))
                .andExpect(jsonPath("$.list.todos", hasSize(1)))
                .andExpect(jsonPath("$.list.todos[0].task", equalTo("this and that")))
                .andExpect(jsonPath("$.list.todos[0]._links", not(isEmptyString())))
                .andExpect(jsonPath("$.list.todos[0]._links.delete.href", not(isEmptyString())))
                .andExpect(jsonPath("$.list.todos[0]._links.update.href", not(isEmptyString())))
                .andExpect(jsonPath("$.list.todos[0]._links.complete.href", not(isEmptyString())))
                .andExpect(jsonPath("$.list.todos[0]._links.move[0].href", not(isEmptyString())))
                .andExpect(jsonPath("$.list.deferredTodos", hasSize(2)))
                .andExpect(jsonPath("$.list.deferredTodos[0].task", equalTo("here and there")))
                .andExpect(jsonPath("$.list.deferredTodos[0]._links", not(isEmptyString())))
                .andExpect(jsonPath("$.list.deferredTodos[0]._links.delete.href", not(isEmptyString())))
                .andExpect(jsonPath("$.list.deferredTodos[0]._links.update.href", not(isEmptyString())))
                .andExpect(jsonPath("$.list.deferredTodos[0]._links.complete.href", not(isEmptyString())))
                .andExpect(jsonPath("$.list.deferredTodos[0]._links.move[0].href", not(isEmptyString())))
                .andExpect(jsonPath("$.list.deferredTodos[0]._links.move[1].href", not(isEmptyString())))
                .andExpect(jsonPath("$.list.deferredTodos[1].task", equalTo("near and far")))
                .andExpect(jsonPath("$.list.deferredTodos[1]._links.move[0].href", not(isEmptyString())))
                .andExpect(jsonPath("$.list.deferredTodos[1]._links.move[1].href", not(isEmptyString())));
    }

    @Test
    public void defaultListActions() throws Exception {
        mockMvc.perform(get("/v1/lists/default")
                .headers(httpHeaders))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list._links", not(Matchers.isEmptyString())))
                .andExpect(jsonPath("$.list._links.createDeferred.href", not(isEmptyString())))
                .andExpect(jsonPath("$.list._links.unlock.href", not(isEmptyString())))
                .andExpect(jsonPath("$.list._links.completed.href", not(isEmptyString())))
                .andExpect(jsonPath("$._links", not(isEmptyString())))
                .andExpect(jsonPath("$._links.self.href", containsString("/v1/list")));
    }

    @Test
    public void listActions_whenListHasCapacity() throws Exception {
        String nextActionHref = JsonPath.parse(mockMvc.perform(get("/v1/lists/default")
                .headers(httpHeaders))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).read("$.list._links.unlock.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(post(nextActionHref)
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString()).read("$._links.list.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(get(nextActionHref)
                .headers(httpHeaders))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).read("$.list._links.create.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(post(nextActionHref)
                .content("{\"task\":\"this and that\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString()).read("$.list._links.createDeferred.href", String.class);
        mockMvc.perform(post(nextActionHref)
                .content("{\"task\":\"here and there\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
                .andExpect(jsonPath("$.list._links", not(Matchers.isEmptyString())))
                .andExpect(jsonPath("$.list._links.create.href", not(isEmptyString())))
                .andExpect(jsonPath("$.list._links.pull.href", not(isEmptyString())));
    }

    @Test
    public void listActions_whenListDoesNotHaveCapacity() throws Exception {
        String nextActionHref = JsonPath.parse(mockMvc.perform(get("/v1/lists/default")
                .headers(httpHeaders))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).read("$.list._links.unlock.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(post(nextActionHref)
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString()).read("$._links.list.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(get(nextActionHref)
                .headers(httpHeaders))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).read("$.list._links.create.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(post(nextActionHref)
                .content("{\"task\":\"this and that\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString()).read("$.list._links.create.href", String.class);
        nextActionHref = JsonPath.parse(mockMvc.perform(post(nextActionHref)
                .content("{\"task\":\"one and two\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString()).read("$.list._links.createDeferred.href", String.class);
        mockMvc.perform(post(nextActionHref)
                .content("{\"task\":\"here and there\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders))
                .andExpect(jsonPath("$.list._links", not(Matchers.isEmptyString())))
                .andExpect(jsonPath("$.list._links.displace.href", not(isEmptyString())))
                .andExpect(jsonPath("$.list._links.escalate.href", not(isEmptyString())));
    }
}
