package integration;

import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import com.jayway.jsonpath.JsonPath;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class EndToEndIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private final HttpHeaders httpHeaders = new HttpHeaders();

    @Autowired
    private UserSessionsApiService userSessionsApiService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        SessionTokenDTO signupSessionToken = userSessionsApiService.signup("test@email.com", "password");
        httpHeaders.add("Session-Token", signupSessionToken.getToken());
    }

    @Test
    public void retrieving_creating_updating_moving_andDeletingTodos() throws Exception {
        String jsonResponse = mockMvc.perform(get("/v1/resources/todo")
            .headers(httpHeaders))
            .andReturn().getResponse().getContentAsString();

        String listHref = JsonPath.parse(jsonResponse).read("$._links.list.href", String.class);
        jsonResponse = mockMvc.perform(
            get(listHref)
                .headers(httpHeaders))
            .andExpect(jsonPath("$.list.todos", hasSize(0)))
            .andReturn().getResponse().getContentAsString();

        String createTodoHref = JsonPath.parse(jsonResponse).read("$.list._links.create.href", String.class);
        mockMvc.perform(
            post(createTodoHref)
                .content("{\"task\":\"original task\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders));
        mockMvc.perform(
            post(createTodoHref)
                .content("{\"task\":\"some other task\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders));

        jsonResponse = mockMvc.perform(
            get(listHref)
                .headers(httpHeaders))
            .andExpect(jsonPath("$.list.todos", hasSize(2)))
            .andExpect(jsonPath("$.list.todos[0].task", equalTo("some other task")))
            .andExpect(jsonPath("$.list.todos[1].task", equalTo("original task")))
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(
            put(
                JsonPath.parse(jsonResponse)
                    .read("$.list.todos[1]._links.update.href", String.class))
                .content("{\"task\":\"updated task\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders));

        jsonResponse = mockMvc.perform(
            get(listHref)
                .headers(httpHeaders))
            .andExpect(jsonPath("$.list.todos[1].task", equalTo("updated task")))
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(post(
            JsonPath.parse(jsonResponse)
                .read("$.list.todos[0]._links.move[1].href", String.class))
            .headers(httpHeaders));

        jsonResponse = mockMvc.perform(
            get(listHref)
                .headers(httpHeaders))
            .andExpect(jsonPath("$.list.todos", hasSize(2)))
            .andExpect(jsonPath("$.list.todos[0].task", equalTo("updated task")))
            .andExpect(jsonPath("$.list.todos[1].task", equalTo("some other task")))
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(delete(
            JsonPath.parse(jsonResponse)
                .read("$.list.todos[1]._links.delete.href", String.class))
            .headers(httpHeaders));

        mockMvc.perform(get(listHref)
            .headers(httpHeaders))
            .andExpect(jsonPath("$.list.todos", hasSize(1)))
            .andExpect(jsonPath("$.list.todos[0].task", equalTo("updated task")));
    }

    @Test
    public void completingTodos_movesThemFromTheList_toTheCompletedList() throws Exception {
        String jsonResponse = mockMvc.perform(get("/v1/resources/history")
            .headers(httpHeaders))
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(get(
            JsonPath.parse(jsonResponse)
                .read("$._links.completedList.href", String.class))
            .headers(httpHeaders))
            .andExpect(jsonPath("$.list.todos", hasSize(0)));

        jsonResponse = mockMvc.perform(get("/v1/resources/todo")
            .headers(httpHeaders))
            .andReturn().getResponse().getContentAsString();

        String listHref = JsonPath.parse(jsonResponse).read("$._links.list.href", String.class);
        jsonResponse = mockMvc.perform(get(listHref)
            .headers(httpHeaders))
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(
            post(JsonPath.parse(jsonResponse).read("$.list._links.create.href", String.class))
                .content("{\"task\":\"some task\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders));

        jsonResponse = mockMvc.perform(get(listHref)
            .headers(httpHeaders))
            .andExpect(jsonPath("$.list.todos", hasSize(1)))
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(post(
            JsonPath.parse(jsonResponse)
                .read("$.list.todos[0]._links.complete.href", String.class))
            .headers(httpHeaders));

        mockMvc.perform(get(listHref)
            .headers(httpHeaders))
            .andExpect(jsonPath("$.list.todos", hasSize(0)));

        jsonResponse = mockMvc.perform(get("/v1/resources/history")
            .headers(httpHeaders))
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(get(
            JsonPath.parse(jsonResponse)
                .read("$._links.completedList.href", String.class))
            .headers(httpHeaders))
            .andExpect(jsonPath("$.list.todos", hasSize(1)))
            .andExpect(jsonPath("$.list.todos[0].task", equalTo("some task")));
    }

    @Test
    public void unlocking_andRetrievingDeferredTodos() throws Exception {
        String jsonResponse = mockMvc.perform(get("/v1/resources/todo")
            .headers(httpHeaders))
            .andReturn().getResponse().getContentAsString();

        String listHref = JsonPath.parse(jsonResponse).read("$._links.list.href", String.class);
        jsonResponse = mockMvc.perform(get(listHref)
            .headers(httpHeaders))
            .andExpect(jsonPath("$.list.unlockDuration", equalTo(0)))
            .andExpect(jsonPath("$.list._links", hasKey("unlock")))
            .andExpect(jsonPath("$.list.deferredTodos", hasSize(0)))
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(post(
            JsonPath.parse(jsonResponse).read("$.list._links.createDeferred.href", String.class))
            .content("{\"task\":\"some task\"}")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(httpHeaders));

        mockMvc.perform(post(
            JsonPath.parse(jsonResponse)
                .read("$.list._links.unlock.href", String.class))
            .headers(httpHeaders));

        mockMvc.perform(get(listHref)
            .headers(httpHeaders))
            .andExpect(jsonPath("$.list.unlockDuration", greaterThan(0)))
            .andExpect(jsonPath("$.list._links", not(hasKey("unlock"))))
            .andExpect(jsonPath("$.list.deferredTodos", hasSize(1)))
            .andExpect(jsonPath("$.list.deferredTodos[0].task", equalTo("some task")));
    }

    @Test
    public void pullingDeferredTodos() throws Exception {
        String jsonResponse = mockMvc.perform(get("/v1/resources/todo")
            .headers(httpHeaders))
            .andReturn().getResponse().getContentAsString();

        String listHref = JsonPath.parse(jsonResponse).read("$._links.list.href", String.class);
        jsonResponse = mockMvc.perform(get(listHref)
            .headers(httpHeaders))
            .andExpect(jsonPath("$.list._links", not(hasKey("pull"))))
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(
            post(
                JsonPath.parse(jsonResponse)
                    .read("$.list._links.create.href", String.class))
                .content("{\"task\":\"task for now\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders));

        mockMvc.perform(
            post(
                JsonPath.parse(jsonResponse)
                    .read("$.list._links.createDeferred.href", String.class))
                .content("{\"task\":\"task for later\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders));

        jsonResponse = mockMvc.perform(
            get(listHref)
                .headers(httpHeaders))
            .andExpect(jsonPath("$.list.todos", hasSize(1)))
            .andExpect(jsonPath("$.list.todos[0].task", equalTo("task for now")))
            .andExpect(jsonPath("$.list._links", hasKey("pull")))
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(
            post(
                JsonPath.parse(jsonResponse)
                    .read("$.list._links.pull.href", String.class))
                .headers(httpHeaders));

        mockMvc.perform(
            get(listHref)
                .headers(httpHeaders))
            .andExpect(jsonPath("$.list.todos", hasSize(2)))
            .andExpect(jsonPath("$.list.todos[0].task", equalTo("task for now")))
            .andExpect(jsonPath("$.list.todos[1].task", equalTo("task for later")));
    }

    @Test
    public void escalatingDeferredTodos() throws Exception {
        String jsonResponse = mockMvc.perform(get("/v1/resources/todo")
            .headers(httpHeaders))
            .andReturn().getResponse().getContentAsString();

        String listHref = JsonPath.parse(jsonResponse).read("$._links.list.href", String.class);
        jsonResponse = mockMvc.perform(get(listHref)
            .headers(httpHeaders))
            .andExpect(jsonPath("$.list._links", not(hasKey("pull"))))
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(
            post(
                JsonPath.parse(jsonResponse)
                    .read("$.list._links.create.href", String.class))
                .content("{\"task\":\"first added task for now\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders));

        mockMvc.perform(
            post(
                JsonPath.parse(jsonResponse)
                    .read("$.list._links.create.href", String.class))
                .content("{\"task\":\"second added task for now\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders));

        mockMvc.perform(
            post(
                JsonPath.parse(jsonResponse)
                    .read("$.list._links.createDeferred.href", String.class))
                .content("{\"task\":\"task for later\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders));

        jsonResponse = mockMvc.perform(
            get(listHref)
                .headers(httpHeaders))
            .andExpect(jsonPath("$.list.todos", hasSize(2)))
            .andExpect(jsonPath("$.list.todos[0].task", equalTo("second added task for now")))
            .andExpect(jsonPath("$.list.todos[1].task", equalTo("first added task for now")))
            .andExpect(jsonPath("$.list._links", hasKey("escalate")))
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(
            post(
                JsonPath.parse(jsonResponse)
                    .read("$.list._links.escalate.href", String.class))
                .headers(httpHeaders));

        mockMvc.perform(
            get(listHref)
                .headers(httpHeaders))
            .andExpect(jsonPath("$.list.todos", hasSize(2)))
            .andExpect(jsonPath("$.list.todos[0].task", equalTo("second added task for now")))
            .andExpect(jsonPath("$.list.todos[1].task", equalTo("task for later")));
    }

    @Test
    public void displacingTodos_defersTodos() throws Exception {
        String jsonResponse = mockMvc.perform(get("/v1/resources/todo")
            .headers(httpHeaders))
            .andReturn().getResponse().getContentAsString();

        String listHref = JsonPath.parse(jsonResponse).read("$._links.list.href", String.class);
        jsonResponse = mockMvc.perform(get(listHref)
            .headers(httpHeaders))
            .andExpect(jsonPath("$.list._links", hasKey("create")))
            .andExpect(jsonPath("$.list._links", not(hasKey("displace"))))
            .andReturn().getResponse().getContentAsString();

        String createTodoHref = JsonPath.parse(jsonResponse).read("$.list._links.create.href", String.class);
        String createDeferredTodoHref = JsonPath.parse(jsonResponse).read("$.list._links.createDeferred.href", String.class);
        String unlockHref = JsonPath.parse(jsonResponse).read("$.list._links.unlock.href", String.class);

        mockMvc.perform(
            post(createTodoHref)
                .content("{\"task\":\"first task for now\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders));

        mockMvc.perform(
            get(listHref)
                .headers(httpHeaders))
            .andExpect(jsonPath("$.list.todos", hasSize(1)))
            .andExpect(jsonPath("$.list._links", hasKey("create")))
            .andExpect(jsonPath("$.list._links", not(hasKey("displace"))));

        mockMvc.perform(
            post(createTodoHref)
                .content("{\"task\":\"second task for now\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders));

        mockMvc.perform(
            post(createDeferredTodoHref)
                .content("{\"task\":\"first task for later\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders));

        jsonResponse = mockMvc.perform(
            get(listHref)
                .headers(httpHeaders))
            .andExpect(jsonPath("$.list.todos", hasSize(2)))
            .andExpect(jsonPath("$.list._links", not(hasKey("create"))))
            .andExpect(jsonPath("$.list._links", hasKey("displace")))
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(
            post(
                JsonPath.parse(jsonResponse)
                    .read("$.list._links.displace.href", String.class))
                .content("{\"task\":\"displacing task\"}")
                .headers(httpHeaders));

        mockMvc.perform(
            get(listHref)
                .headers(httpHeaders))
            .andExpect(jsonPath("$.list.todos", hasSize(2)))
            .andExpect(jsonPath("$.list.todos[0].task", equalTo("displacing task")))
            .andExpect(jsonPath("$.list.todos[1].task", equalTo("second task for now")));

        mockMvc.perform(
            post(unlockHref)
                .headers(httpHeaders));

        mockMvc.perform(get(listHref)
            .headers(httpHeaders))
            .andExpect(jsonPath("$.list.deferredTodos", hasSize(2)))
            .andExpect(jsonPath("$.list.deferredTodos[0].task", equalTo("first task for now")))
            .andExpect(jsonPath("$.list.deferredTodos[1].task", equalTo("first task for later")));
    }
}
