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

  private val todoListValue = TodoListModel(listId, "someName", List(), new Date(0L), 0)

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
    val todoIdToMove1 = new TodoId("someDeferredTodoIdentifier1")
    val todoIdToMove2 = new TodoId("someDeferredTodoIdentifier2")
    val todoIdToDelete = new TodoId("deleteMe")
    val todoIdToUpdate = new TodoId("updateMe")
    val todoIdToComplete = new TodoId("completeMe")
    val setupPlan = List(
      (todoListValue: TodoListModel) => TodoListModel.add(todoListValue, new TodoId("someTodoIdentifier"), "someTask"),
      (todoListValue: TodoListModel) => TodoListModel.addDeferred(todoListValue, todoIdToMove1, "someDeferredTask1"),
      (todoListValue: TodoListModel) => TodoListModel.addDeferred(todoListValue, todoIdToMove2, "someDeferredTask2"),
      (todoListValue: TodoListModel) => TodoListModel.move(todoListValue, todoIdToMove1, todoIdToMove2),
      (todoListValue: TodoListModel) => TodoListModel.addDeferred(todoListValue, todoIdToDelete, "taskToDelete"),
      (todoListValue: TodoListModel) => TodoListModel.delete(todoListValue, todoIdToDelete),
      (todoListValue: TodoListModel) => TodoListModel.addDeferred(todoListValue, todoIdToUpdate, "taskToUpdate"),
      (todoListValue: TodoListModel) => TodoListModel.update(todoListValue, todoIdToUpdate, "updatedTask"),
      (todoListValue: TodoListModel) => TodoListModel.pull(todoListValue),
      (todoListValue: TodoListModel) => TodoListModel.escalate(todoListValue),
      (todoListValue: TodoListModel) => TodoListModel.displace(todoListValue, todoIdToComplete, "someImportantTask"),
      (todoListValue: TodoListModel) => TodoListModel.complete(todoListValue, todoIdToComplete),
      (todoListValue: TodoListModel) => TodoListModel.unlock(todoListValue, Date.from(Instant.now()))
    )
    val todoListEvents: List[TodoListEvent] = List()
    val (resultingTodoListValue, resultingTodoListEvents) = setupPlan.foldLeft((todoListValue, todoListEvents))((accumTuple, function) => {
      val (todoListValue, todoListEvent) = function.apply(accumTuple._1).get
      (todoListValue, accumTuple._2 :+ todoListEvent)
    })

    todoListEventRepository.saveAll(userId, listId, resultingTodoListEvents)

    val retrievedTodoListModel = todoListModelRepository.find(userId, listId).get
    assertThat(retrievedTodoListModel).isEqualTo(resultingTodoListValue)
  }

  @Test
  def savesExistingTodoList(): Unit = {
    val todoIdToUpdate = new TodoId("someTodoIdentifier")
    val setupPlan = List(
      (todoListValue: TodoListModel) => TodoListModel.add(todoListValue, todoIdToUpdate, "someTask"),
      (todoListValue: TodoListModel) => TodoListModel.addDeferred(todoListValue, new TodoId("someDeferredTodoIdentifier1"), "someDeferredTask1")
    )
    val todoListEvents: List[TodoListEvent] = List()
    val (_, resultingTodoListEvents) = setupPlan.foldLeft((todoListValue, todoListEvents))((accumTuple, function) => {
      val (todoListValue, todoListEvent) = function.apply(accumTuple._1).get
      (todoListValue, accumTuple._2 :+ todoListEvent)
    })

    todoListEventRepository.saveAll(userId, listId, resultingTodoListEvents)

    val retrievedTodoListModel1 = todoListModelRepository.find(userId, listId).get

    val (resultingTodoListModel, event) = TodoListModel.update(retrievedTodoListModel1, todoIdToUpdate, "newTask").get

    todoListEventRepository.save(userId, listId, event)

    val retrievedTodoListModel2 = todoListModelRepository.find(userId, listId).get
    assertThat(retrievedTodoListModel2).isEqualTo(resultingTodoListModel)
  }
}