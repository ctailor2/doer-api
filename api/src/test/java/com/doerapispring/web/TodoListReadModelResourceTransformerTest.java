package com.doerapispring.web;

import com.doerapispring.domain.ListId;
import com.doerapispring.domain.Todo;
import com.doerapispring.domain.TodoId;
import com.doerapispring.domain.TodoListReadModel;
import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.Link;

import java.util.List;

import static com.doerapispring.web.MockHateoasLinkGenerator.MOCK_BASE_URL;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TodoListReadModelResourceTransformerTest {
    private final TodoListReadModelResourceTransformer todoListReadModelResourceTransformer = new TodoListReadModelResourceTransformer(new MockHateoasLinkGenerator());

    private final TodoListReadModel todoListReadModel = mock(TodoListReadModel.class);
    private String listId;

    @Before
    public void setUp() throws Exception {
        ListId listId = new ListId("someListId");
        this.listId = listId.get();
        when(todoListReadModel.getListId()).thenReturn(listId);
    }

    @Test
    public void show_returnsList() {
        String profileName = "someProfileName";
        when(todoListReadModel.getProfileName()).thenReturn(profileName);
        String name = "someName";
        when(todoListReadModel.getSectionName()).thenReturn(name);
        String deferredName = "someDeferredName";
        when(todoListReadModel.getDeferredSectionName()).thenReturn(deferredName);
        Todo todo = new Todo(new TodoId("oneNowId"), "oneNowTask");
        when(todoListReadModel.getTodos()).thenReturn(singletonList(todo));
        Todo deferredTodo = new Todo(new TodoId("oneLaterId"), "oneLaterTask");
        when(todoListReadModel.getDeferredTodos()).thenReturn(singletonList(deferredTodo));
        long unlockDuration = 123213L;
        when(todoListReadModel.unlockDuration()).thenReturn(unlockDuration);

        TodoListReadModelResponse responseEntity = todoListReadModelResourceTransformer.transform(todoListReadModel);

        TodoListReadModelDTO todoListReadModelDTO = responseEntity.getTodoListReadModelDTO();
        assertThat(todoListReadModelDTO).isNotNull();
        assertThat(todoListReadModelDTO.getProfileName()).isEqualTo(profileName);
        assertThat(todoListReadModelDTO.getName()).isEqualTo(name);
        assertThat(todoListReadModelDTO.getDeferredName()).isEqualTo(deferredName);
        assertThat(todoListReadModelDTO.getTodos()).hasSize(1);
        assertThat(todoListReadModelDTO.getTodos().get(0).getIdentifier()).isEqualTo(todo.getTodoId().getIdentifier());
        assertThat(todoListReadModelDTO.getTodos().get(0).getTask()).isEqualTo(todo.getTask());
        assertThat(todoListReadModelDTO.getDeferredTodos()).hasSize(1);
        assertThat(todoListReadModelDTO.getDeferredTodos().get(0).getIdentifier()).isEqualTo(deferredTodo.getTodoId().getIdentifier());
        assertThat(todoListReadModelDTO.getDeferredTodos().get(0).getTask()).isEqualTo(deferredTodo.getTask());
        assertThat(todoListReadModelDTO.getUnlockDuration()).isEqualTo(unlockDuration);
    }

    @Test
    public void show_includesLinks_byDefault() {
        TodoListReadModelResponse responseEntity = todoListReadModelResourceTransformer.transform(todoListReadModel);

        assertThat(responseEntity.getLinks()).contains(new Link(MOCK_BASE_URL + "/lists/" + listId).withSelfRel());
        List<Link> links = responseEntity.getTodoListReadModelDTO().getLinks();
        assertThat(links).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/createDeferredTodo").withRel("createDeferred"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/completedList").withRel("completed"));
    }

    @Test
    public void show_returnsList_includesLinksForEachTodo() {
        List<Todo> todos = asList(
            new Todo(new TodoId("oneNowId"), "oneNowTask"),
            new Todo(new TodoId("twoNowId"), "twoNowTask"));
        when(todoListReadModel.getTodos()).thenReturn(todos);
        List<Todo> deferredTodos = asList(
            new Todo(new TodoId("oneLaterId"), "oneLaterTask"),
            new Todo(new TodoId("twoLaterId"), "twoLaterTask"));
        when(todoListReadModel.getDeferredTodos()).thenReturn(deferredTodos);

        TodoListReadModelResponse responseEntity = todoListReadModelResourceTransformer.transform(todoListReadModel);

        assertThat(responseEntity.getTodoListReadModelDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/createDeferredTodo").withRel("createDeferred"));
        assertThat(responseEntity.getTodoListReadModelDTO().getTodos()).hasSize(2);
        assertThat(responseEntity.getTodoListReadModelDTO().getTodos().get(0).getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/deleteTodo/oneNowId").withRel("delete"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/updateTodo/oneNowId").withRel("update"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/completeTodo/oneNowId").withRel("complete"));
        assertThat(responseEntity.getTodoListReadModelDTO().getTodos().get(0).getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/deleteTodo/oneNowId").withRel("delete"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/updateTodo/oneNowId").withRel("update"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/completeTodo/oneNowId").withRel("complete"));
        assertThat(responseEntity.getTodoListReadModelDTO().getTodos().get(0).getLinks()).containsSequence(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/todos/oneNowId/moveTodo/oneNowId").withRel("move"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/todos/oneNowId/moveTodo/twoNowId").withRel("move"));
        assertThat(responseEntity.getTodoListReadModelDTO().getTodos().get(1).getLinks()).containsSequence(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/todos/twoNowId/moveTodo/oneNowId").withRel("move"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/todos/twoNowId/moveTodo/twoNowId").withRel("move"));
        assertThat(responseEntity.getTodoListReadModelDTO().getDeferredTodos()).hasSize(2);
        assertThat(responseEntity.getTodoListReadModelDTO().getDeferredTodos().get(0).getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/deleteTodo/oneLaterId").withRel("delete"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/updateTodo/oneLaterId").withRel("update"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/completeTodo/oneLaterId").withRel("complete"));
        assertThat(responseEntity.getTodoListReadModelDTO().getDeferredTodos().get(0).getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/deleteTodo/oneLaterId").withRel("delete"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/updateTodo/oneLaterId").withRel("update"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/completeTodo/oneLaterId").withRel("complete"));
        assertThat(responseEntity.getTodoListReadModelDTO().getDeferredTodos().get(0).getLinks()).containsSequence(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/todos/oneLaterId/moveTodo/oneLaterId").withRel("move"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/todos/oneLaterId/moveTodo/twoLaterId").withRel("move"));
        assertThat(responseEntity.getTodoListReadModelDTO().getDeferredTodos().get(1).getLinks()).containsSequence(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/todos/twoLaterId/moveTodo/oneLaterId").withRel("move"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/todos/twoLaterId/moveTodo/twoLaterId").withRel("move"));
    }

    @Test
    public void show_whenListIsNotFull_includesCreateLink_doesNotIncludeDisplaceLink() {
        when(todoListReadModel.isFull()).thenReturn(false);

        TodoListReadModelResponse responseEntity = todoListReadModelResourceTransformer.transform(todoListReadModel);

        assertThat(responseEntity.getTodoListReadModelDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/createTodo").withRel("create"));
        assertThat(responseEntity.getTodoListReadModelDTO().getLinks()).doesNotContain(
            new Link(MOCK_BASE_URL + "/list/displaceTodo").withRel("displace"));
    }

    @Test
    public void show_whenListIsFull_doesNotIncludeCreateLink_includesDisplaceLink() {
        when(todoListReadModel.isFull()).thenReturn(true);

        TodoListReadModelResponse responseEntity = todoListReadModelResourceTransformer.transform(todoListReadModel);

        assertThat(responseEntity.getTodoListReadModelDTO().getLinks()).doesNotContain(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/createTodo").withRel("create"));
        assertThat(responseEntity.getTodoListReadModelDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/displaceTodo").withRel("displace"));
    }

    @Test
    public void show_whenListIsAbleToBeReplenished_includesPullLink() {
        when(todoListReadModel.isAbleToBeReplenished()).thenReturn(true);

        TodoListReadModelResponse responseEntity = todoListReadModelResourceTransformer.transform(todoListReadModel);

        assertThat(responseEntity.getTodoListReadModelDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/pullTodos").withRel("pull"));
    }

    @Test
    public void show_whenListIsNotAbleToBeReplenished_doesNotIncludePullLink() {
        when(todoListReadModel.isAbleToBeReplenished()).thenReturn(false);

        TodoListReadModelResponse responseEntity = todoListReadModelResourceTransformer.transform(todoListReadModel);

        assertThat(responseEntity.getTodoListReadModelDTO().getLinks()).doesNotContain(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/pullTodos").withRel("pull"));
    }

    @Test
    public void show_whenListIsAbleToBeUnlocked_includesUnlockLink() {
        when(todoListReadModel.isAbleToBeUnlocked()).thenReturn(true);

        TodoListReadModelResponse responseEntity = todoListReadModelResourceTransformer.transform(todoListReadModel);

        assertThat(responseEntity.getTodoListReadModelDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/unlockTodos").withRel("unlock"));
    }

    @Test
    public void show_whenListIsNotAbleToBeUnlocked_doesNotIncludeUnlockLink() {
        when(todoListReadModel.isAbleToBeUnlocked()).thenReturn(false);

        TodoListReadModelResponse responseEntity = todoListReadModelResourceTransformer.transform(todoListReadModel);

        assertThat(responseEntity.getTodoListReadModelDTO().getLinks()).doesNotContain(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/unlockTodos").withRel("unlock"));
    }

    @Test
    public void show_whenListIsAbleToBeEscalated_includesEscalateLink() {
        when(todoListReadModel.isAbleToBeEscalated()).thenReturn(true);

        TodoListReadModelResponse responseEntity = todoListReadModelResourceTransformer.transform(todoListReadModel);

        assertThat(responseEntity.getTodoListReadModelDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/escalateTodo").withRel("escalate"));
    }

    @Test
    public void show_whenListIsNotAbleToBeEscalated_doesNotIncludeEscalateLink() {
        when(todoListReadModel.isAbleToBeEscalated()).thenReturn(false);

        TodoListReadModelResponse responseEntity = todoListReadModelResourceTransformer.transform(todoListReadModel);

        assertThat(responseEntity.getTodoListReadModelDTO().getLinks()).doesNotContain(
            new Link(MOCK_BASE_URL + "/list/escalateTodo").withRel("escalate"));
    }
}