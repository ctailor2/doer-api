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
  private val todoListDao: TodoListDao = null

  @Autowired
  private val todoListEventStoreRepository: TodoListEventStoreRepository = null

  @Autowired
  private val userRepository: UserRepository = null

  @Autowired
  private val todoListRepository: TodoListRepository = null

  @Autowired
  private val todoListEventRepository: TodoListEventRepository = null

  @Autowired
  private val objectMapper: ObjectMapper = null

  private var todoList: TodoList = _

  private val userId: UserId = new UserId("someUserIdentifier")

  private val listId: ListId = new ListId("someListIdentifier")

  private val todoListValue = TodoListModel(listId, "someName")

  @Before
  @throws[Exception]
  def setUp(): Unit = {
    userRepository.save(new User(userId, listId))
    todoList = new TodoList(userId, listId, "someName")
    todoListRepository.save(todoList)
    todoListModelRepository = new TodoListModelEventSourcedRepository(todoListDao, todoListEventStoreRepository, objectMapper)
  }

  @Test
  def savesTodoList(): Unit = {
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
}