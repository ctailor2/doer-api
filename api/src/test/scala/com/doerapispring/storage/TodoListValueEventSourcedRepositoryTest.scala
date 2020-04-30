package com.doerapispring.storage

import java.time.Instant
import java.util.Date

import com.doerapispring.domain._
import com.doerapispring.domain.events.TodoListEvent
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
class TodoListValueEventSourcedRepositoryTest {
  private var todoListValueRepository: TodoListValueEventSourcedRepository = _

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

  private var userId: UserId = _

  private var listId: ListId = _

  private val todoListValue = TodoListValue(List(), new Date(0L), 0)

  @Before
  @throws[Exception]
  def setUp(): Unit = {
    userId = new UserId("someUserIdentifier")
    listId = new ListId("someListIdentifier")
    userRepository.save(new User(userId, listId))
    todoList = new TodoList(userId, listId, "someName", 0, new Date(0L))
    todoListRepository.save(todoList)
    todoListValueRepository = new TodoListValueEventSourcedRepository(todoListDao, todoListEventStoreRepository, objectMapper)
  }

  @Test
  def savesTodoList(): Unit = {
    val todoIdToMove1 = new TodoId("someDeferredTodoIdentifier1")
    val todoIdToMove2 = new TodoId("someDeferredTodoIdentifier2")
    val todoIdToDelete = new TodoId("deleteMe")
    val todoIdToUpdate = new TodoId("updateMe")
    val todoIdToComplete = new TodoId("completeMe")
    val setupPlan = List(
      (todoListValue: TodoListValue) => TodoListValue.add(todoListValue, new TodoId("someTodoIdentifier"), "someTask"),
      (todoListValue: TodoListValue) => TodoListValue.addDeferred(todoListValue, todoIdToMove1, "someDeferredTask1"),
      (todoListValue: TodoListValue) => TodoListValue.addDeferred(todoListValue, todoIdToMove2, "someDeferredTask2"),
      (todoListValue: TodoListValue) => TodoListValue.move(todoListValue, todoIdToMove1, todoIdToMove2),
      (todoListValue: TodoListValue) => TodoListValue.addDeferred(todoListValue, todoIdToDelete, "taskToDelete"),
      (todoListValue: TodoListValue) => TodoListValue.delete(todoListValue, todoIdToDelete),
      (todoListValue: TodoListValue) => TodoListValue.addDeferred(todoListValue, todoIdToUpdate, "taskToUpdate"),
      (todoListValue: TodoListValue) => TodoListValue.update(todoListValue, todoIdToUpdate, "updatedTask"),
      (todoListValue: TodoListValue) => TodoListValue.pull(todoListValue),
      (todoListValue: TodoListValue) => TodoListValue.escalate(todoListValue),
      (todoListValue: TodoListValue) => TodoListValue.displace(todoListValue, todoIdToComplete, "someImportantTask"),
      (todoListValue: TodoListValue) => TodoListValue.complete(todoListValue, todoIdToComplete),
      (todoListValue: TodoListValue) => TodoListValue.unlock(todoListValue, Date.from(Instant.now()))
    )
    val todoListEvents: List[TodoListEvent] = List()
    val (resultingTodoListValue, resultingTodoListEvents) = setupPlan.foldLeft((todoListValue, todoListEvents))((accumTuple, function) => {
      val (todoListValue, todoListEvent) = function.apply(accumTuple._1).get
      (todoListValue, accumTuple._2 :+ todoListEvent)
    })

    todoListEventRepository.saveAll(userId, listId, resultingTodoListEvents)

    val retrievedTodoListValue = todoListValueRepository.find(userId, listId).get
    assertThat(retrievedTodoListValue).isEqualTo(resultingTodoListValue)
  }

  @Test
  def savesExistingTodoList(): Unit = {
    val todoIdToUpdate = new TodoId("someTodoIdentifier")
    val setupPlan = List(
      (todoListValue: TodoListValue) => TodoListValue.add(todoListValue, todoIdToUpdate, "someTask"),
      (todoListValue: TodoListValue) => TodoListValue.addDeferred(todoListValue, new TodoId("someDeferredTodoIdentifier1"), "someDeferredTask1")
    )
    val todoListEvents: List[TodoListEvent] = List()
    val (_, resultingTodoListEvents) = setupPlan.foldLeft((todoListValue, todoListEvents))((accumTuple, function) => {
      val (todoListValue, todoListEvent) = function.apply(accumTuple._1).get
      (todoListValue, accumTuple._2 :+ todoListEvent)
    })

    todoListEventRepository.saveAll(userId, listId, resultingTodoListEvents)

    val retrievedTodoListValue1 = todoListValueRepository.find(userId, listId).get

    val (resultingTodoListValue, event) = TodoListValue.update(retrievedTodoListValue1, todoIdToUpdate, "newTask").get

    todoListEventRepository.save(userId, listId, event)

    val retrievedTodoListValue2 = todoListValueRepository.find(userId, listId).get
    assertThat(retrievedTodoListValue2).isEqualTo(resultingTodoListValue)
  }
}