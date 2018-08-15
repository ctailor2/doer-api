package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedAuthenticationToken;
import com.doerapispring.authentication.AuthenticatedUser;
import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.doerapispring.web.MockHateoasLinkGenerator.MOCK_BASE_URL;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ListsControllerTest {
    private ListsController listsController;

    private ListApiService mockListApiService;

    private MockMvc mockMvc;
    private AuthenticatedUser authenticatedUser;

    @Before
    public void setUp() throws Exception {
        mockListApiService = mock(ListApiService.class);
        String identifier = "test@email.com";
        authenticatedUser = new AuthenticatedUser(identifier);
        SecurityContextHolder.getContext().setAuthentication(new AuthenticatedAuthenticationToken(authenticatedUser));
        listsController = new ListsController(new MockHateoasLinkGenerator(), mockListApiService);
        mockMvc = MockMvcBuilders
            .standaloneSetup(listsController)
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();
    }

    @Test
    public void unlock_mapping() throws Exception {
        mockMvc.perform(post("/v1/list/unlock"))
            .andExpect(status().isAccepted());

        verify(mockListApiService).unlock(authenticatedUser);
    }

    @Test
    public void unlock_callsTodoService_returns202() throws Exception {
        ResponseEntity<ResourcesResponse> responseEntity = listsController.unlock(authenticatedUser);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/list/unlockTodos").withSelfRel(),
            new Link(MOCK_BASE_URL + "/list").withRel("list"));
    }

    @Test
    public void unlock_whenInvalidRequest_returns400BadRequest() throws Exception {
        doThrow(new InvalidRequestException()).when(mockListApiService).unlock(any());

        ResponseEntity<ResourcesResponse> responseEntity = listsController.unlock(authenticatedUser);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void show_mapping() throws Exception {
        when(mockListApiService.get(any())).thenReturn(new MasterListDTO("someName", "someDeferredName", emptyList(), emptyList(), 0L, false, false, false));

        mockMvc.perform(get("/v1/list"))
            .andExpect(status().isOk());

        verify(mockListApiService).get(authenticatedUser);
    }

    @Test
    public void show_returnsList_includesLinks_byDefault() throws Exception {
        MasterListDTO masterListDTO = new MasterListDTO("someName", "someDeferredName", emptyList(), emptyList(), 0L, false, false, false);
        when(mockListApiService.get(any())).thenReturn(masterListDTO);

        ResponseEntity<MasterListResponse> responseEntity = listsController.show(authenticatedUser);

        assertThat(responseEntity.getBody().getMasterListDTO()).isEqualTo(masterListDTO);
        assertThat(responseEntity.getBody().getLinks()).contains(new Link(MOCK_BASE_URL + "/list").withSelfRel());
        assertThat(responseEntity.getBody().getMasterListDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/list/createDeferredTodo").withRel("createDeferred"));
    }

    @Test
    public void show_returnsList_includesLinksForEachTodo() throws Exception {
        MasterListDTO masterListDTO = new MasterListDTO("someName",
            "someDeferredName",
            asList(
                new TodoDTO("oneNowId", "oneNowTask"),
                new TodoDTO("twoNowId", "twoNowTask")),
            asList(
                new TodoDTO("oneLaterId", "oneLaterTask"),
                new TodoDTO("twoLaterId", "twoLaterTask")),
            0L,
            false,
            false,
            false);
        when(mockListApiService.get(any())).thenReturn(masterListDTO);

        ResponseEntity<MasterListResponse> responseEntity = listsController.show(authenticatedUser);

        assertThat(responseEntity.getBody().getMasterListDTO()).isEqualTo(masterListDTO);
        assertThat(responseEntity.getBody().getLinks()).contains(new Link(MOCK_BASE_URL + "/list").withSelfRel());
        assertThat(responseEntity.getBody().getMasterListDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/list/createDeferredTodo").withRel("createDeferred"));
        assertThat(responseEntity.getBody().getMasterListDTO().getTodos()).hasSize(2);
        assertThat(responseEntity.getBody().getMasterListDTO().getTodos().get(0).getLinks()).contains(
            new Link(MOCK_BASE_URL + "/deleteTodo/oneNowId").withRel("delete"),
            new Link(MOCK_BASE_URL + "/updateTodo/oneNowId").withRel("update"),
            new Link(MOCK_BASE_URL + "/completeTodo/oneNowId").withRel("complete"));
        assertThat(responseEntity.getBody().getMasterListDTO().getTodos().get(0).getLinks()).contains(
            new Link(MOCK_BASE_URL + "/deleteTodo/oneNowId").withRel("delete"),
            new Link(MOCK_BASE_URL + "/updateTodo/oneNowId").withRel("update"),
            new Link(MOCK_BASE_URL + "/completeTodo/oneNowId").withRel("complete"));
        assertThat(responseEntity.getBody().getMasterListDTO().getTodos().get(0).getLinks()).containsSequence(
            new Link(MOCK_BASE_URL + "/todos/oneNowId/moveTodo/oneNowId").withRel("move"),
            new Link(MOCK_BASE_URL + "/todos/oneNowId/moveTodo/twoNowId").withRel("move"));
        assertThat(responseEntity.getBody().getMasterListDTO().getTodos().get(1).getLinks()).containsSequence(
            new Link(MOCK_BASE_URL + "/todos/twoNowId/moveTodo/oneNowId").withRel("move"),
            new Link(MOCK_BASE_URL + "/todos/twoNowId/moveTodo/twoNowId").withRel("move"));
        assertThat(responseEntity.getBody().getMasterListDTO().getDeferredTodos()).hasSize(2);
        assertThat(responseEntity.getBody().getMasterListDTO().getDeferredTodos().get(0).getLinks()).contains(
            new Link(MOCK_BASE_URL + "/deleteTodo/oneLaterId").withRel("delete"),
            new Link(MOCK_BASE_URL + "/updateTodo/oneLaterId").withRel("update"),
            new Link(MOCK_BASE_URL + "/completeTodo/oneLaterId").withRel("complete"));
        assertThat(responseEntity.getBody().getMasterListDTO().getDeferredTodos().get(0).getLinks()).contains(
            new Link(MOCK_BASE_URL + "/deleteTodo/oneLaterId").withRel("delete"),
            new Link(MOCK_BASE_URL + "/updateTodo/oneLaterId").withRel("update"),
            new Link(MOCK_BASE_URL + "/completeTodo/oneLaterId").withRel("complete"));
        assertThat(responseEntity.getBody().getMasterListDTO().getDeferredTodos().get(0).getLinks()).containsSequence(
            new Link(MOCK_BASE_URL + "/todos/oneLaterId/moveTodo/oneLaterId").withRel("move"),
            new Link(MOCK_BASE_URL + "/todos/oneLaterId/moveTodo/twoLaterId").withRel("move"));
        assertThat(responseEntity.getBody().getMasterListDTO().getDeferredTodos().get(1).getLinks()).containsSequence(
            new Link(MOCK_BASE_URL + "/todos/twoLaterId/moveTodo/oneLaterId").withRel("move"),
            new Link(MOCK_BASE_URL + "/todos/twoLaterId/moveTodo/twoLaterId").withRel("move"));
    }

    @Test
    public void show_whenListIsNotFull_includesCreateLink_doesNotIncludeDisplaceLink() throws Exception {
        when(mockListApiService.get(any())).thenReturn(new MasterListDTO("someName", "someDeferredName", emptyList(), emptyList(), 0L, false, false, false));

        ResponseEntity<MasterListResponse> responseEntity = listsController.show(authenticatedUser);

        assertThat(responseEntity.getBody().getMasterListDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/list/createTodo").withRel("create"));
        assertThat(responseEntity.getBody().getMasterListDTO().getLinks()).doesNotContain(
            new Link(MOCK_BASE_URL + "/list/displaceTodo").withRel("displace"));
    }

    @Test
    public void show_whenListIsFull_doesNotIncludeCreateLink_includesDisplaceLink() throws Exception {
        when(mockListApiService.get(any())).thenReturn(new MasterListDTO("someName", "someDeferredName", emptyList(), emptyList(), 0L, true, false, false));

        ResponseEntity<MasterListResponse> responseEntity = listsController.show(authenticatedUser);

        assertThat(responseEntity.getBody().getMasterListDTO().getLinks()).doesNotContain(
            new Link(MOCK_BASE_URL + "/list/createTodo").withRel("create"));
        assertThat(responseEntity.getBody().getMasterListDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/list/displaceTodo").withRel("displace"));
    }

    @Test
    public void show_whenListIsAbleToBeReplenished_includesPullLink() throws Exception {
        when(mockListApiService.get(any())).thenReturn(
            new MasterListDTO("someName", "someDeferredName", emptyList(), emptyList(), 0L, false, false, true));

        ResponseEntity<MasterListResponse> responseEntity = listsController.show(authenticatedUser);

        assertThat(responseEntity.getBody().getMasterListDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/list/pullTodos").withRel("pull"));
    }

    @Test
    public void show_whenListIsNotAbleToBeReplenished_doesNotIncludePullLink() throws Exception {
        when(mockListApiService.get(any())).thenReturn(
            new MasterListDTO("someName", "someDeferredName", emptyList(), emptyList(), 0L, false, false, false));

        ResponseEntity<MasterListResponse> responseEntity = listsController.show(authenticatedUser);

        assertThat(responseEntity.getBody().getMasterListDTO().getLinks()).doesNotContain(
            new Link(MOCK_BASE_URL + "/list/pullTodos").withRel("pull"));
    }

    @Test
    public void show_whenListIsAbleToBeUnlocked_includesUnlockLink() throws Exception {
        when(mockListApiService.get(any())).thenReturn(new MasterListDTO("someName", "someDeferredName", emptyList(), emptyList(), 0L, false, true, false));

        ResponseEntity<MasterListResponse> responseEntity = listsController.show(authenticatedUser);

        assertThat(responseEntity.getBody().getMasterListDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/list/unlockTodos").withRel("unlock"));
    }

    @Test
    public void show_whenListIsNotAbleToBeUnlocked_doesNotIncludeUnlockLink() throws Exception {
        when(mockListApiService.get(any())).thenReturn(new MasterListDTO("someName", "someDeferredName", emptyList(), emptyList(), 0L, false, false, false));

        ResponseEntity<MasterListResponse> responseEntity = listsController.show(authenticatedUser);

        assertThat(responseEntity.getBody().getMasterListDTO().getLinks()).doesNotContain(
            new Link(MOCK_BASE_URL + "/list/unlockTodos").withRel("unlock"));
    }

    @Test
    public void show_whenListIsLocked_doesNotIncludeDeferredTodosLink() throws Exception {
        when(mockListApiService.get(any())).thenReturn(new MasterListDTO("someName", "someDeferredName", emptyList(), emptyList(), 0L, false, false, false));

        ResponseEntity<MasterListResponse> responseEntity = listsController.show(authenticatedUser);

        assertThat(responseEntity.getBody().getMasterListDTO().getLinks()).doesNotContain(
            new Link(MOCK_BASE_URL + "/list/deferredTodos").withRel("deferredTodos"));
    }

    @Test
    public void show_whenInvalidRequest_throws400BadRequest() throws Exception {
        when(mockListApiService.get(any())).thenThrow(new InvalidRequestException());

        ResponseEntity<MasterListResponse> responseEntity = listsController.show(authenticatedUser);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void showCompleted_mapping() throws Exception {
        CompletedListDTO completedListDTO = new CompletedListDTO(emptyList());
        when(mockListApiService.getCompleted(any())).thenReturn(completedListDTO);

        mockMvc.perform(get("/v1/completedList"))
            .andExpect(status().isOk());

        verify(mockListApiService).getCompleted(authenticatedUser);
    }

    @Test
    public void showCompleted_returnsList_includesLinksByDefault() throws Exception {
        CompletedListDTO completedListDTO = new CompletedListDTO(emptyList());
        when(mockListApiService.getCompleted(any())).thenReturn(completedListDTO);

        ResponseEntity<CompletedListResponse> responseEntity = listsController.showCompleted(authenticatedUser);

        assertThat(responseEntity.getBody().getCompletedListDTO()).isEqualTo(completedListDTO);
        assertThat(responseEntity.getBody().getLinks()).contains(new Link(MOCK_BASE_URL + "/completedList").withSelfRel());
        assertThat(responseEntity.getBody().getCompletedListDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/completedTodos").withRel("todos"));
    }

    @Test
    public void showCompleted_whenInvalidRequest_throws400BadRequest() throws Exception {
        when(mockListApiService.getCompleted(any())).thenThrow(new InvalidRequestException());

        ResponseEntity<CompletedListResponse> responseEntity = listsController.showCompleted(authenticatedUser);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}