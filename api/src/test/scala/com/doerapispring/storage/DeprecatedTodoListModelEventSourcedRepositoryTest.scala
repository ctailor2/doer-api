package com.doerapispring.storage

import java.time.Instant
import java.util.Date

import com.doerapispring.domain._
import com.doerapispring.domain.events._
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.runner.RunWith
import org.junit.{Before, Test}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.junit4.SpringRunner

@SpringBootTest
@Sql(scripts = Array("/cleanup.sql"), executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = Array("/cleanup.sql"), executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@ActiveProfiles(value = Array("test"))
@RunWith(classOf[SpringRunner])
class DeprecatedTodoListModelEventSourcedRepositoryTest {
  private var todoListModelRepository: DeprecatedTodoListModelEventSourcedRepository = _

  @Autowired
  private val todoListModelSnapshotRepository: DeprecatedTodoListModelSnapshotRepository = null

  @Autowired
  private val userRepository: UserRepository = null

  @Autowired
  private val todoListEventRepository: TodoListEventRepository = null

  @Autowired
  private val todoListRepository: TodoListRepository = null

  @Autowired
  private val objectMapper: ObjectMapper = null

  @Autowired
  private val jdbcTemplate: JdbcTemplate = null

  private val userId: UserId = new UserId("someUserIdentifier")

  private val listId: ListId = new ListId("someListIdentifier")

  private val todoListValue = DeprecatedTodoListModel(listId, "someName")

  @Before
  @throws[Exception]
  def setUp(): Unit = {
    userRepository.save(new User(userId, listId))
    todoListRepository.save(new TodoList(userId, listId, "someListName"))
    todoListModelRepository = new DeprecatedTodoListModelEventSourcedRepository(todoListModelSnapshotRepository, objectMapper, jdbcTemplate)
  }

  @Test
  def savesTodoList(): Unit = {
    todoListModelSnapshotRepository.save(userId, listId, Snapshot(todoListValue, Date.from(Instant.now())))
    val todoIdToMove1 = "someDeferredTodoIdentifier1"
    val todoIdToMove2 = "someDeferredTodoIdentifier2"
    val todoIdToDelete = "deleteMe"
    val todoIdToUpdate = "updateMe"
    val todoIdToComplete = "completeMe"
    val todoListEvents = List(
      DeprecatedTodoAddedEvent("someTodoIdentifier", "someTask"),
      DeprecatedDeferredTodoAddedEvent(todoIdToMove1, "someDeferredTask1"),
      DeprecatedDeferredTodoAddedEvent(todoIdToMove2, "someDeferredTask2"),
      DeprecatedTodoMovedEvent(todoIdToMove1, todoIdToMove2),
      DeprecatedDeferredTodoAddedEvent(todoIdToDelete, "taskToDelete"),
      DeprecatedTodoDeletedEvent(todoIdToDelete),
      DeprecatedDeferredTodoAddedEvent(todoIdToUpdate, "taskToUpdate"),
      DeprecatedTodoUpdatedEvent(todoIdToUpdate, "updatedTask"),
      DeprecatedPulledEvent(),
      DeprecatedEscalatedEvent(),
      DeprecatedTodoDisplacedEvent(todoIdToComplete, "someImportantTask"),
      DeprecatedTodoCompletedEvent(todoIdToComplete),
      DeprecatedUnlockedEvent(Date.from(Instant.now()))
    )
    val resultingTodoListValue = todoListEvents.foldLeft(todoListValue)((todoListModel, todoListEvent) => {
      DeprecatedTodoListModel.applyEvent(todoListModel, todoListEvent).get
    })

    todoListEventRepository.saveAll(userId, listId, todoListEvents)

    val retrievedTodoListModel = todoListModelRepository.find(userId, listId).get
    assertThat(retrievedTodoListModel).isEqualTo(resultingTodoListValue)
  }

  @Test
  def startsFromTheTodoListSnapshotWhenOneExists(): Unit = {
    val todoListModel = DeprecatedTodoListModel(
      listId,
      "someProfileName",
      List(new DeprecatedTodo(new TodoId("someTodoId"), "someTask")),
      new Date(123L),
      7,
      "someSectionName",
      "someDeferredSectionName")
    todoListModelSnapshotRepository.save(userId, listId, Snapshot(todoListModel, Date.from(Instant.now())))
    val todoAddedEvent = DeprecatedTodoAddedEvent("someOtherTodoId", "someOtherTask")
    todoListEventRepository.save(userId, listId, todoAddedEvent)

    val resultingTodoListValue = DeprecatedTodoListModel.applyEvent(todoListModel, todoAddedEvent).get

    val retrievedTodoListModel = todoListModelRepository.find(userId, listId).get
    assertThat(retrievedTodoListModel).isEqualTo(resultingTodoListValue)
  }

  @Test
  def producesTheModelFromTheEventsThatOccurredAfterTheSnapshot(): Unit = {
    val eventBeforeSnapshot = DeprecatedTodoAddedEvent("someOtherTodoId", "someOtherTask")
    todoListEventRepository.save(userId, listId, eventBeforeSnapshot)

    val todoListModel = DeprecatedTodoListModel(
      listId,
      "someProfileName",
      List(new DeprecatedTodo(new TodoId("someTodoId"), "someTask")),
      new Date(123L),
      7,
      "someSectionName",
      "someDeferredSectionName")
    todoListModelSnapshotRepository.save(userId, listId, Snapshot(todoListModel, Date.from(Instant.now())))

    val eventAfterSnapshot = DeprecatedDeferredTodoAddedEvent("yetAnotherTodoId", "yetAnotherTask")
    todoListEventRepository.save(userId, listId, eventAfterSnapshot)

    val resultingTodoListValue = DeprecatedTodoListModel.applyEvent(todoListModel, eventAfterSnapshot).get

    val retrievedTodoListModel = todoListModelRepository.find(userId, listId).get
    assertThat(retrievedTodoListModel).isEqualTo(resultingTodoListValue)
  }
}