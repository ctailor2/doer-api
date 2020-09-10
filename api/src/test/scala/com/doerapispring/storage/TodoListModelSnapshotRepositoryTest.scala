package com.doerapispring.storage

import java.time.Instant
import java.util.Date

import com.doerapispring.domain._
import com.doerapispring.domain.events.DeprecatedTodoAddedEvent
import org.assertj.core.api.Assertions
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
class TodoListModelSnapshotRepositoryTest {

  @Autowired
  private val todoListModelSnapshotRepository: TodoListModelSnapshotRepository = null

  @Autowired
  private val userRepository: UserRepository = null

  @Autowired
  private val todoListRepository: TodoListRepository = null

  private val userId: UserId = new UserId("someUserIdentifier")

  private val listId: ListId = new ListId("someListIdentifier")

  @Before
  @throws[Exception]
  def setUp(): Unit = {
    userRepository.save(new User(userId, listId))
    val todoList = new TodoList(userId, listId, "someName")
    todoListRepository.save(todoList)
  }

  @Test
  def savesTheTodoListModelSnapshot(): Unit = {
    val todoListModel = DeprecatedTodoListModel(
        listId,
      "someProfileName",
      List(new DeprecatedTodo(new TodoId("someTodoId"), "someTask")),
      new Date(123L),
      7,
      "someSectionName",
      "someDeferredSectionName")
    val todoListModelSnapshot = Snapshot(todoListModel, Date.from(Instant.now()))

    todoListModelSnapshotRepository.save(userId, listId, todoListModelSnapshot)

    val actualTodoListModelSnapshot = todoListModelSnapshotRepository.find(userId, listId)

    Assertions.assertThat(actualTodoListModelSnapshot.get).isEqualTo(todoListModelSnapshot)
  }

  @Test
  def updatesTheTodoListModelSnapshot(): Unit = {
    val todoListModel = DeprecatedTodoListModel(
      listId,
      "someProfileName",
      List(new DeprecatedTodo(new TodoId("someTodoId"), "someTask")),
      new Date(123L),
      7,
      "someSectionName",
      "someDeferredSectionName")
    val todoListModelSnapshot = Snapshot(todoListModel, Date.from(Instant.now()))

    todoListModelSnapshotRepository.save(userId, listId, todoListModelSnapshot)

    val updatedTodoListModel = DeprecatedTodoListModel.applyEvent(todoListModel, DeprecatedTodoAddedEvent("someOtherTodoId", "someOtherTask")).get
    val updatedTodoListModelSnapshot = Snapshot(updatedTodoListModel, Date.from(Instant.now()))
    todoListModelSnapshotRepository.save(userId, listId, updatedTodoListModelSnapshot)

    val actualTodoListModelSnapshot = todoListModelSnapshotRepository.find(userId, listId)

    Assertions.assertThat(actualTodoListModelSnapshot.get).isEqualTo(updatedTodoListModelSnapshot)
  }
}
