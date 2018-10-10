package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedAuthenticationToken;
import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.ListApplicationService;
import com.doerapispring.domain.ReadOnlyMasterList;
import com.doerapispring.domain.Todo;
import com.doerapispring.domain.TodoId;
import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static com.doerapispring.web.MockHateoasLinkGenerator.MOCK_BASE_URL;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ListsControllerTest {
    private ListsController listsController;

    private ListApplicationService listApplicationService;

    private MockMvc mockMvc;
    private AuthenticatedUser authenticatedUser;
    private ReadOnlyMasterList readOnlyMasterList;

    @Before
    public void setUp() throws Exception {
        listApplicationService = mock(ListApplicationService.class);
        String identifier = "test@email.com";
        authenticatedUser = new AuthenticatedUser(identifier);
        SecurityContextHolder.getContext().setAuthentication(new AuthenticatedAuthenticationToken(authenticatedUser));
        listsController = new ListsController(new MockHateoasLinkGenerator(), listApplicationService);
        mockMvc = MockMvcBuilders
            .standaloneSetup(listsController)
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();

        readOnlyMasterList = mock(ReadOnlyMasterList.class);
        when(listApplicationService.get(any())).thenReturn(readOnlyMasterList);
    }

    @Test
    public void unlock_mapping() throws Exception {
        mockMvc.perform(post("/v1/list/unlock"))
            .andExpect(status().isAccepted());

        verify(listApplicationService).unlock(authenticatedUser.getUser());
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
        doThrow(new InvalidRequestException()).when(listApplicationService).unlock(any());

        ResponseEntity<ResourcesResponse> responseEntity = listsController.unlock(authenticatedUser);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void show_mapping() throws Exception {
        mockMvc.perform(get("/v1/list"))
            .andExpect(status().isOk());

        verify(listApplicationService).get(authenticatedUser.getUser());
    }

    @Test
    public void show_returnsList() throws Exception {
        String name = "someName";
        when(readOnlyMasterList.getName()).thenReturn(name);
        String deferredName = "someDeferredName";
        when(readOnlyMasterList.getDeferredName()).thenReturn(deferredName);
        Todo todo = new Todo(new TodoId("oneNowId"), "oneNowTask");
        when(readOnlyMasterList.getTodos()).thenReturn(singletonList(todo));
        Todo deferredTodo = new Todo(new TodoId("oneLaterId"), "oneLaterTask");
        when(readOnlyMasterList.getDeferredTodos()).thenReturn(singletonList(deferredTodo));
        long unlockDuration = 123213L;
        when(readOnlyMasterList.unlockDuration()).thenReturn(unlockDuration);

        ResponseEntity<MasterListResponse> responseEntity = listsController.show(authenticatedUser);

        MasterListDTO masterListDTO = responseEntity.getBody().getMasterListDTO();
        assertThat(masterListDTO).isNotNull();
        assertThat(masterListDTO.getName()).isEqualTo(name);
        assertThat(masterListDTO.getDeferredName()).isEqualTo(deferredName);
        assertThat(masterListDTO.getTodos()).hasSize(1);
        assertThat(masterListDTO.getTodos().get(0).getIdentifier()).isEqualTo(todo.getTodoId().getIdentifier());
        assertThat(masterListDTO.getTodos().get(0).getTask()).isEqualTo(todo.getTask());
        assertThat(masterListDTO.getDeferredTodos()).hasSize(1);
        assertThat(masterListDTO.getDeferredTodos().get(0).getIdentifier()).isEqualTo(deferredTodo.getTodoId().getIdentifier());
        assertThat(masterListDTO.getDeferredTodos().get(0).getTask()).isEqualTo(deferredTodo.getTask());
        assertThat(masterListDTO.getUnlockDuration()).isEqualTo(unlockDuration);
    }

    @Test
    public void show_includesLinks_byDefault() throws Exception {
        ResponseEntity<MasterListResponse> responseEntity = listsController.show(authenticatedUser);

        assertThat(responseEntity.getBody().getLinks()).contains(new Link(MOCK_BASE_URL + "/list").withSelfRel());
        assertThat(responseEntity.getBody().getMasterListDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/list/createDeferredTodo").withRel("createDeferred"));
    }

    @Test
    public void show_returnsList_includesLinksForEachTodo() throws Exception {
        List<Todo> todos = asList(
            new Todo(new TodoId("oneNowId"), "oneNowTask"),
            new Todo(new TodoId("twoNowId"), "twoNowTask"));
        when(readOnlyMasterList.getTodos()).thenReturn(todos);
        List<Todo> deferredTodos = asList(
            new Todo(new TodoId("oneLaterId"), "oneLaterTask"),
            new Todo(new TodoId("twoLaterId"), "twoLaterTask"));
        when(readOnlyMasterList.getDeferredTodos()).thenReturn(deferredTodos);

        ResponseEntity<MasterListResponse> responseEntity = listsController.show(authenticatedUser);

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
        when(readOnlyMasterList.isFull()).thenReturn(false);

        ResponseEntity<MasterListResponse> responseEntity = listsController.show(authenticatedUser);

        assertThat(responseEntity.getBody().getMasterListDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/list/createTodo").withRel("create"));
        assertThat(responseEntity.getBody().getMasterListDTO().getLinks()).doesNotContain(
            new Link(MOCK_BASE_URL + "/list/displaceTodo").withRel("displace"));
    }

    @Test
    public void show_whenListIsFull_doesNotIncludeCreateLink_includesDisplaceLink() throws Exception {
        when(readOnlyMasterList.isFull()).thenReturn(true);

        ResponseEntity<MasterListResponse> responseEntity = listsController.show(authenticatedUser);

        assertThat(responseEntity.getBody().getMasterListDTO().getLinks()).doesNotContain(
            new Link(MOCK_BASE_URL + "/list/createTodo").withRel("create"));
        assertThat(responseEntity.getBody().getMasterListDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/list/displaceTodo").withRel("displace"));
    }

    @Test
    public void show_whenListIsAbleToBeReplenished_includesPullLink() throws Exception {
        when(readOnlyMasterList.isAbleToBeReplenished()).thenReturn(true);

        ResponseEntity<MasterListResponse> responseEntity = listsController.show(authenticatedUser);

        assertThat(responseEntity.getBody().getMasterListDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/list/pullTodos").withRel("pull"));
    }

    @Test
    public void show_whenListIsNotAbleToBeReplenished_doesNotIncludePullLink() throws Exception {
        when(readOnlyMasterList.isAbleToBeReplenished()).thenReturn(false);

        ResponseEntity<MasterListResponse> responseEntity = listsController.show(authenticatedUser);

        assertThat(responseEntity.getBody().getMasterListDTO().getLinks()).doesNotContain(
            new Link(MOCK_BASE_URL + "/list/pullTodos").withRel("pull"));
    }

    @Test
    public void show_whenListIsAbleToBeUnlocked_includesUnlockLink() throws Exception {
        when(readOnlyMasterList.isAbleToBeUnlocked()).thenReturn(true);

        ResponseEntity<MasterListResponse> responseEntity = listsController.show(authenticatedUser);

        assertThat(responseEntity.getBody().getMasterListDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/list/unlockTodos").withRel("unlock"));
    }

    @Test
    public void show_whenListIsNotAbleToBeUnlocked_doesNotIncludeUnlockLink() throws Exception {
        when(readOnlyMasterList.isAbleToBeUnlocked()).thenReturn(false);

        ResponseEntity<MasterListResponse> responseEntity = listsController.show(authenticatedUser);

        assertThat(responseEntity.getBody().getMasterListDTO().getLinks()).doesNotContain(
            new Link(MOCK_BASE_URL + "/list/unlockTodos").withRel("unlock"));
    }

    @Test
    public void show_whenInvalidRequest_throws400BadRequest() throws Exception {
        when(listApplicationService.get(any())).thenThrow(new InvalidRequestException());

        ResponseEntity<MasterListResponse> responseEntity = listsController.show(authenticatedUser);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void showCompleted_mapping() throws Exception {
        CompletedListDTO completedListDTO = new CompletedListDTO(emptyList());
        when(listApplicationService.getCompleted(any())).thenReturn(completedListDTO);

        mockMvc.perform(get("/v1/completedList"))
            .andExpect(status().isOk());

        verify(listApplicationService).getCompleted(authenticatedUser.getUser());
    }

    @Test
    public void showCompleted_returnsList_includesLinksByDefault() throws Exception {
        CompletedListDTO completedListDTO = new CompletedListDTO(emptyList());
        when(listApplicationService.getCompleted(any())).thenReturn(completedListDTO);

        ResponseEntity<CompletedListResponse> responseEntity = listsController.showCompleted(authenticatedUser);

        assertThat(responseEntity.getBody().getCompletedListDTO()).isEqualTo(completedListDTO);
        assertThat(responseEntity.getBody().getLinks()).contains(new Link(MOCK_BASE_URL + "/completedList").withSelfRel());
    }

    @Test
    public void showCompleted_whenInvalidRequest_throws400BadRequest() throws Exception {
        when(listApplicationService.getCompleted(any())).thenThrow(new InvalidRequestException());

        ResponseEntity<CompletedListResponse> responseEntity = listsController.showCompleted(authenticatedUser);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}