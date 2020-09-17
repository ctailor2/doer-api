package com.doerapispring.storage

import java.time.Instant
import java.util.Date

import com.doerapispring.domain._
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions
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
class AbstractTodoListModelSnapshotRepositoryTest {

  private var todoListModelSnapshotRepository: AbstractTodoListModelSnapshotRepository[TestTodoListModel] = null

  @Autowired
  private val userRepository: UserRepository = null

  @Autowired
  private val todoListRepository: TodoListRepository = null

  private val userId: UserId = new UserId("someUserIdentifier")

  private val listId: ListId = new ListId("someListIdentifier")

  @Autowired
  private val jdbcTemplate: JdbcTemplate = null

  @Autowired
  private val objectMapper: ObjectMapper = null

  @Before
  @throws[Exception]
  def setUp(): Unit = {
    todoListModelSnapshotRepository = new TestTodoListModelSnapshotRepository(jdbcTemplate, objectMapper)
    userRepository.save(new User(userId, listId))
    val todoList = new TodoList(userId, listId, "someName")
    todoListRepository.save(todoList)
  }

  @Test
  def savesTheTodoListModelSnapshot(): Unit = {
    val todoListModel = TestTodoListModel("someValue")
    val todoListModelSnapshot = Snapshot(todoListModel, Date.from(Instant.now()))

    todoListModelSnapshotRepository.save(userId, listId, todoListModelSnapshot)

    val actualTodoListModelSnapshot = todoListModelSnapshotRepository.find(userId, listId)

    Assertions.assertThat(actualTodoListModelSnapshot.get).isEqualTo(todoListModelSnapshot)
  }

  @Test
  def updatesTheTodoListModelSnapshot(): Unit = {
    val todoListModel = TestTodoListModel("someValue")
    val todoListModelSnapshot = Snapshot(todoListModel, Date.from(Instant.now()))

    todoListModelSnapshotRepository.save(userId, listId, todoListModelSnapshot)

    val updatedTodoListModel = TestTodoListModel("someUpdatedValue")
    val updatedTodoListModelSnapshot = Snapshot(updatedTodoListModel, Date.from(Instant.now()))
    todoListModelSnapshotRepository.save(userId, listId, updatedTodoListModelSnapshot)

    val actualTodoListModelSnapshot = todoListModelSnapshotRepository.find(userId, listId)

    Assertions.assertThat(actualTodoListModelSnapshot.get).isEqualTo(updatedTodoListModelSnapshot)
  }
}

case class TestTodoListModel(someAttribute: String)

class TestTodoListModelSnapshotRepository(jdbcTemplate: JdbcTemplate,
                                          objectMapper: ObjectMapper)
  extends AbstractTodoListModelSnapshotRepository[TestTodoListModel](classOf[TestTodoListModel], jdbcTemplate, objectMapper)