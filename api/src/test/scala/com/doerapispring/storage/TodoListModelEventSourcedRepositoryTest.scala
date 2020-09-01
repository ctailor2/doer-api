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
class TodoListModelEventSourcedRepositoryTest {
  private var todoListModelRepository: TodoListModelEventSourcedRepository = _

  @Autowired
  private val todoListModelSnapshotRepository: TodoListModelSnapshotRepository = null

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

  private val todoListValue = TodoListModel(listId, "someName")

  @Before
  @throws[Exception]
  def setUp(): Unit = {
    userRepository.save(new User(userId, listId))
    todoListRepository.save(new TodoList(userId, listId, "someListName"))
    todoListModelRepository = new TodoListModelEventSourcedRepository(todoListModelSnapshotRepository, objectMapper, jdbcTemplate)
  }

  @Test
  def savesTodoList(): Unit = {
    todoListModelSnapshotRepository.save(userId, listId, TodoListModelSnapshot(todoListValue, Date.from(Instant.now())))
    val todoIdToMove1 = "someDeferredTodoIdentifier1"
    val todoIdToMove2 = "someDeferredTodoIdentifier2"
    val todoIdToDelete = "deleteMe"
    val todoIdToUpdate = "updateMe"
    val todoIdToComplete = "completeMe"
    val todoListEvents = List(
      TodoAddedEvent("someTodoIdentifier", "someTask"),
      DeferredTodoAddedEvent(todoIdToMove1, "someDeferredTask1"),
      DeferredTodoAddedEvent(todoIdToMove2, "someDeferredTask2"),
      TodoMovedEvent(todoIdToMove1, todoIdToMove2),
      DeferredTodoAddedEvent(todoIdToDelete, "taskToDelete"),
      TodoDeletedEvent(todoIdToDelete),
      DeferredTodoAddedEvent(todoIdToUpdate, "taskToUpdate"),
      TodoUpdatedEvent(todoIdToUpdate, "updatedTask"),
      PulledEvent(),
      EscalatedEvent(),
      TodoDisplacedEvent(todoIdToComplete, "someImportantTask"),
      TodoCompletedEvent(todoIdToComplete),
      UnlockedEvent(Date.from(Instant.now()))
    )
    val resultingTodoListValue = todoListEvents.foldLeft(todoListValue)((todoListModel, todoListEvent) => {
      TodoListModel.applyEvent(todoListModel, todoListEvent).get
    })

    todoListEventRepository.saveAll(userId, listId, todoListEvents)

    val retrievedTodoListModel = todoListModelRepository.find(userId, listId).get
    assertThat(retrievedTodoListModel).isEqualTo(resultingTodoListValue)
  }

  @Test
  def startsFromTheTodoListSnapshotWhenOneExists(): Unit = {
    val todoListModel = TodoListModel(
      listId,
      "someProfileName",
      List(new Todo(new TodoId("someTodoId"), "someTask")),
      new Date(123L),
      7,
      "someSectionName",
      "someDeferredSectionName")
    todoListModelSnapshotRepository.save(userId, listId, TodoListModelSnapshot(todoListModel, Date.from(Instant.now())))
    val todoAddedEvent = TodoAddedEvent("someOtherTodoId", "someOtherTask")
    todoListEventRepository.save(userId, listId, todoAddedEvent)

    val resultingTodoListValue = TodoListModel.applyEvent(todoListModel, todoAddedEvent).get

    val retrievedTodoListModel = todoListModelRepository.find(userId, listId).get
    assertThat(retrievedTodoListModel).isEqualTo(resultingTodoListValue)
  }

  @Test
  def producesTheModelFromTheEventsThatOccurredAfterTheSnapshot(): Unit = {
    val eventBeforeSnapshot = TodoAddedEvent("someOtherTodoId", "someOtherTask")
    todoListEventRepository.save(userId, listId, eventBeforeSnapshot)

    val todoListModel = TodoListModel(
      listId,
      "someProfileName",
      List(new Todo(new TodoId("someTodoId"), "someTask")),
      new Date(123L),
      7,
      "someSectionName",
      "someDeferredSectionName")
    todoListModelSnapshotRepository.save(userId, listId, TodoListModelSnapshot(todoListModel, Date.from(Instant.now())))

    val eventAfterSnapshot = DeferredTodoAddedEvent("yetAnotherTodoId", "yetAnotherTask")
    todoListEventRepository.save(userId, listId, eventAfterSnapshot)

    val resultingTodoListValue = TodoListModel.applyEvent(todoListModel, eventAfterSnapshot).get

    val retrievedTodoListModel = todoListModelRepository.find(userId, listId).get
    assertThat(retrievedTodoListModel).isEqualTo(resultingTodoListValue)
  }
}