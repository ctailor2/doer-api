package com.doerapispring.web

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

import com.doerapispring.domain.{ListId, TodoId, TodoListModel}
import com.doerapispring.web.MockHateoasLinkGenerator.MOCK_BASE_URL
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.hateoas.Link


class TodoListModelResourceTransformerTest {
  final private val todoListReadModelResourceTransformer = new TodoListModelResourceTransformer(new MockHateoasLinkGenerator)
  final private val listId = "someListId"
  final private val profileName = "someProfileName"
  final private val sectionName = "someName"
  final private val deferredSectionName = "someDeferredName"
  final private val todoListModel = new TodoListModel(new ListId(listId), profileName, List(), Date.from(Instant.EPOCH), 0, sectionName, deferredSectionName)

  @Test
  def show_returnsList(): Unit = {
    val todoId = new TodoId("oneNowId")
    val task = "oneNowTask"
    val deferredTodoId = new TodoId("oneLaterId")
    val deferredTask = "oneLaterTask"
    val todoListModel = TodoListModel.add(this.todoListModel, todoId, task)
      .flatMap(todoListModel => TodoListModel.addDeferred(todoListModel, deferredTodoId, deferredTask))
      .flatMap(todoListModel => TodoListModel.unlock(todoListModel, Date.from(Instant.now())))
      .get
    val responseEntity = todoListReadModelResourceTransformer.transform(todoListModel, Date.from(Instant.now()))
    val todoListReadModelDTO = responseEntity.getTodoListReadModelDTO
    assertThat(todoListReadModelDTO).isNotNull
    assertThat(todoListReadModelDTO.getProfileName).isEqualTo(profileName)
    assertThat(todoListReadModelDTO.getName).isEqualTo(sectionName)
    assertThat(todoListReadModelDTO.getDeferredName).isEqualTo(deferredSectionName)
    assertThat(todoListReadModelDTO.getTodos).hasSize(1)
    assertThat(todoListReadModelDTO.getTodos.get(0).getIdentifier).isEqualTo(todoId.getIdentifier)
    assertThat(todoListReadModelDTO.getTodos.get(0).getTask).isEqualTo(task)
    assertThat(todoListReadModelDTO.getDeferredTodos).hasSize(1)
    assertThat(todoListReadModelDTO.getDeferredTodos.get(0).getIdentifier).isEqualTo(deferredTodoId.getIdentifier)
    assertThat(todoListReadModelDTO.getDeferredTodos.get(0).getTask).isEqualTo(deferredTask)
  }

    @Test
    def show_returnsListUnlockDuration(): Unit = {
      val currentInstant = Instant.now()
      val delta = 1676771L
      val unlockDuration = 123229L
      val todoList = TodoListModel.unlock(this.todoListModel, Date.from(currentInstant.minusMillis(delta))).get

      val responseEntity = todoListReadModelResourceTransformer.transform(todoList, Date.from(currentInstant))

      val todoListReadModelDTO = responseEntity.getTodoListReadModelDTO
      assertThat(todoListReadModelDTO.getUnlockDuration).isEqualTo(unlockDuration)
    }

  @Test def show_includesLinks_byDefault(): Unit = {
    val responseEntity = todoListReadModelResourceTransformer.transform(this.todoListModel, Date.from(Instant.now()))
    val links = responseEntity.getTodoListReadModelDTO.getLinks
    assertThat(links).contains(new Link(MOCK_BASE_URL + "/lists/" + listId + "/createDeferredTodo").withRel("createDeferred"), new Link(MOCK_BASE_URL + "/lists/" + listId + "/completedList").withRel("completed"))
  }

  @Test def show_returnsList_includesLinksForEachTodo(): Unit = {
    val todoListModel = TodoListModel.add(this.todoListModel, new TodoId("twoNowId"), "twoNowTask")
      .flatMap(todoListModel => TodoListModel.add(todoListModel, new TodoId("oneNowId"), "oneNowTask"))
      .flatMap(todoListModel => TodoListModel.addDeferred(todoListModel, new TodoId("oneLaterId"), "oneLaterTask"))
      .flatMap(todoListModel => TodoListModel.addDeferred(todoListModel, new TodoId("twoLaterId"), "twoLaterTask"))
      .flatMap(todoListModel => TodoListModel.unlock(todoListModel, Date.from(Instant.now())))
      .get

    val responseEntity = todoListReadModelResourceTransformer.transform(todoListModel, Date.from(Instant.now()))
    assertThat(responseEntity.getTodoListReadModelDTO.getLinks).contains(new Link(MOCK_BASE_URL + "/lists/" + listId + "/createDeferredTodo").withRel("createDeferred"))
    assertThat(responseEntity.getTodoListReadModelDTO.getTodos).hasSize(2)
    assertThat(responseEntity.getTodoListReadModelDTO.getTodos.get(0).getLinks).contains(new Link(MOCK_BASE_URL + "/lists/" + listId + "/deleteTodo/oneNowId").withRel("delete"), new Link(MOCK_BASE_URL + "/lists/" + listId + "/updateTodo/oneNowId").withRel("update"), new Link(MOCK_BASE_URL + "/lists/" + listId + "/completeTodo/oneNowId").withRel("complete"))
    assertThat(responseEntity.getTodoListReadModelDTO.getTodos.get(0).getLinks).contains(new Link(MOCK_BASE_URL + "/lists/" + listId + "/deleteTodo/oneNowId").withRel("delete"), new Link(MOCK_BASE_URL + "/lists/" + listId + "/updateTodo/oneNowId").withRel("update"), new Link(MOCK_BASE_URL + "/lists/" + listId + "/completeTodo/oneNowId").withRel("complete"))
    assertThat(responseEntity.getTodoListReadModelDTO.getTodos.get(0).getLinks).containsSequence(new Link(MOCK_BASE_URL + "/lists/" + listId + "/todos/oneNowId/moveTodo/oneNowId").withRel("move"), new Link(MOCK_BASE_URL + "/lists/" + listId + "/todos/oneNowId/moveTodo/twoNowId").withRel("move"))
    assertThat(responseEntity.getTodoListReadModelDTO.getTodos.get(1).getLinks).containsSequence(new Link(MOCK_BASE_URL + "/lists/" + listId + "/todos/twoNowId/moveTodo/oneNowId").withRel("move"), new Link(MOCK_BASE_URL + "/lists/" + listId + "/todos/twoNowId/moveTodo/twoNowId").withRel("move"))
    assertThat(responseEntity.getTodoListReadModelDTO.getDeferredTodos).hasSize(2)
    assertThat(responseEntity.getTodoListReadModelDTO.getDeferredTodos.get(0).getLinks).contains(new Link(MOCK_BASE_URL + "/lists/" + listId + "/deleteTodo/oneLaterId").withRel("delete"), new Link(MOCK_BASE_URL + "/lists/" + listId + "/updateTodo/oneLaterId").withRel("update"), new Link(MOCK_BASE_URL + "/lists/" + listId + "/completeTodo/oneLaterId").withRel("complete"))
    assertThat(responseEntity.getTodoListReadModelDTO.getDeferredTodos.get(0).getLinks).contains(new Link(MOCK_BASE_URL + "/lists/" + listId + "/deleteTodo/oneLaterId").withRel("delete"), new Link(MOCK_BASE_URL + "/lists/" + listId + "/updateTodo/oneLaterId").withRel("update"), new Link(MOCK_BASE_URL + "/lists/" + listId + "/completeTodo/oneLaterId").withRel("complete"))
    assertThat(responseEntity.getTodoListReadModelDTO.getDeferredTodos.get(0).getLinks).containsSequence(new Link(MOCK_BASE_URL + "/lists/" + listId + "/todos/oneLaterId/moveTodo/oneLaterId").withRel("move"), new Link(MOCK_BASE_URL + "/lists/" + listId + "/todos/oneLaterId/moveTodo/twoLaterId").withRel("move"))
    assertThat(responseEntity.getTodoListReadModelDTO.getDeferredTodos.get(1).getLinks).containsSequence(new Link(MOCK_BASE_URL + "/lists/" + listId + "/todos/twoLaterId/moveTodo/oneLaterId").withRel("move"), new Link(MOCK_BASE_URL + "/lists/" + listId + "/todos/twoLaterId/moveTodo/twoLaterId").withRel("move"))
  }

    @Test def show_whenListHasCreateCapability_includesCreateLink(): Unit = {
      val responseEntity = todoListReadModelResourceTransformer.transform(todoListModel, Date.from(Instant.now()))
      assertThat(responseEntity.getTodoListReadModelDTO.getLinks).contains(new Link(MOCK_BASE_URL + "/lists/" + listId + "/createTodo").withRel("create"))
    }

    @Test def show_whenListHasDisplaceCapability_includesDisplaceLink(): Unit = {
      val todoListModel = TodoListModel.add(this.todoListModel, new TodoId("twoNowId"), "twoNowTask")
        .flatMap(todoListModel => TodoListModel.add(todoListModel, new TodoId("oneNowId"), "oneNowTask"))
        .get
      val responseEntity = todoListReadModelResourceTransformer.transform(todoListModel, Date.from(Instant.now()))
      assertThat(responseEntity.getTodoListReadModelDTO.getLinks).contains(new Link(MOCK_BASE_URL + "/lists/" + listId + "/displaceTodo").withRel("displace"))
    }

    @Test def show_whenListIsAbleToBeReplenished_includesPullLink(): Unit = {
      val todoListModel = TodoListModel.addDeferred(this.todoListModel, new TodoId("someTodoId"), "someTask").get
      val responseEntity = todoListReadModelResourceTransformer.transform(todoListModel, Date.from(Instant.now()))
      assertThat(responseEntity.getTodoListReadModelDTO.getLinks).contains(new Link(MOCK_BASE_URL + "/lists/" + listId + "/pullTodos").withRel("pull"))
    }

    @Test def show_whenListIsNotAbleToBeReplenished_doesNotIncludePullLink(): Unit = {
      val responseEntity = todoListReadModelResourceTransformer.transform(todoListModel, Date.from(Instant.now()))
      assertThat(responseEntity.getTodoListReadModelDTO.getLinks).doesNotContain(new Link(MOCK_BASE_URL + "/lists/" + listId + "/pullTodos").withRel("pull"))
    }

    @Test def show_whenListIsAbleToBeUnlocked_includesUnlockLink(): Unit = {
      val responseEntity = todoListReadModelResourceTransformer.transform(todoListModel, Date.from(Instant.now()))
      assertThat(responseEntity.getTodoListReadModelDTO.getLinks).contains(new Link(MOCK_BASE_URL + "/lists/" + listId + "/unlockTodos").withRel("unlock"))
    }

    @Test def show_whenListIsNotAbleToBeUnlocked_doesNotIncludeUnlockLink(): Unit = {
      val responseEntity = todoListReadModelResourceTransformer.transform(todoListModel, Date.from(Instant.EPOCH.plus(23, ChronoUnit.HOURS)))
      assertThat(responseEntity.getTodoListReadModelDTO.getLinks).doesNotContain(new Link(MOCK_BASE_URL + "/lists/" + listId + "/unlockTodos").withRel("unlock"))
    }

    @Test def show_whenListIsAbleToBeEscalated_includesEscalateLink(): Unit = {
    val todoListModel = TodoListModel.add(this.todoListModel, new TodoId("twoNowId"), "twoNowTask")
      .flatMap(todoListModel => TodoListModel.add(todoListModel, new TodoId("oneNowId"), "oneNowTask"))
      .flatMap(todoListModel => TodoListModel.addDeferred(todoListModel, new TodoId("oneLaterId"), "oneLaterTask"))
      .get
      val responseEntity = todoListReadModelResourceTransformer.transform(todoListModel, Date.from(Instant.now()))
      assertThat(responseEntity.getTodoListReadModelDTO.getLinks).contains(new Link(MOCK_BASE_URL + "/lists/" + listId + "/escalateTodo").withRel("escalate"))
    }

    @Test def show_whenListIsNotAbleToBeEscalated_doesNotIncludeEscalateLink(): Unit = {
      val responseEntity = todoListReadModelResourceTransformer.transform(todoListModel, Date.from(Instant.now()))
      assertThat(TodoListModel.escalateCapability(todoListModel).isFailure).isTrue
      assertThat(responseEntity.getTodoListReadModelDTO.getLinks).doesNotContain(new Link(MOCK_BASE_URL + "/lists/" + listId + "/escalateTodo").withRel("escalate"))
    }
}