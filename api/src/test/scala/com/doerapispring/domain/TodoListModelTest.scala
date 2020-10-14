package com.doerapispring.domain

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

import com.doerapispring.domain.events._
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

import scala.jdk.CollectionConverters._
import scala.util.chaining._

//noinspection AccessorLikeMethodIsUnit
class TodoListModelTest {
  private val todoListValue: TodoListModel = new TodoListModel(scala.collection.immutable.List.empty, scala.collection.immutable.List.empty, Date.from(Instant.EPOCH), 0)

  @Test
  def add_addsToNowList(): Unit = {
    val todoListValue = TodoListModel.add(this.todoListValue, "someTask")
    assertThat(TodoListModel.getTodos(todoListValue).asJavaCollection).containsExactly(Todo("someTask"))
  }

  @Test
  def add_addsToNowList_beforeFirstTodo(): Unit = {
    val todoListValue = TodoListModel.add(this.todoListValue, "someTask")
      .pipe(todoList => TodoListModel.add(todoList, "someOtherTask"))

    assertThat(TodoListModel.getTodos(todoListValue).asJavaCollection).extracting("task").containsExactly("someOtherTask", "someTask")
  }

  @Test
  def addDeferred_addsToLaterList(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue = TodoListModel.addDeferred(this.todoListValue, "someTask")
      .pipe(todoList => TodoListModel.unlock(todoList, now))
    assertThat(TodoListModel.getDeferredTodos(todoListValue, now).asJavaCollection).containsExactly(Todo("someTask"))
  }

  @Test
  def addDeferred_addsToLaterList_afterLastTodo(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue = TodoListModel.addDeferred(this.todoListValue, "someTask")
      .pipe(todoList => TodoListModel.addDeferred(todoList, "someOtherTask"))
      .pipe(todoList => TodoListModel.unlock(todoList, now))
    assertThat(TodoListModel.getDeferredTodos(todoListValue, now).asJavaCollection).extracting("task").containsExactly("someTask", "someOtherTask")
  }

  @Test
  def delete_whenTodoWithIdentifierExists_removesTodo(): Unit = {
    val todoListValue = TodoListModel.add(this.todoListValue, "someTask")
      .pipe(todoList => TodoListModel.delete(todoList, 0))
    assertThat(todoListValue.todos.asJavaCollection).isEmpty()
    assertThat(TodoListModel.getTodos(todoListValue).asJavaCollection).isEmpty()
  }

  @Test
  def delete_whenTodoWithIdentifierExists_removesDeferredTodo(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue = TodoListModel.addDeferred(this.todoListValue, "someTask")
      .pipe(todoList => TodoListModel.delete(todoList, 0))
      .pipe(todoList => TodoListModel.unlock(todoList, now))
    assertThat(todoListValue.todos.asJavaCollection).isEmpty()
    assertThat(TodoListModel.getDeferredTodos(todoListValue, now).asJavaCollection).isEmpty()
  }

  @Test
  def displace_whenPostponedListIsEmpty_replacesTodo_andPushesItIntoPostponedListWithCorrectPositioning(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue = TodoListModel.add(this.todoListValue, "someNowTask")
      .pipe(todoList => TodoListModel.add(todoList, "someOtherNowTask"))
      .pipe(todoList => TodoListModel.displace(todoList, "displace it"))
      .pipe(todoList => TodoListModel.unlock(todoList, now))
    assertThat(TodoListModel.getTodos(todoListValue).asJavaCollection).containsExactly(
      Todo("displace it"),
      Todo("someOtherNowTask"))
    assertThat(TodoListModel.getDeferredTodos(todoListValue, now).asJavaCollection).containsExactly(Todo("someNowTask"))
  }

  @Test
  def displace_whenPostponedIsNotEmpty_replacesTodo_andPushesItIntoPostponedListWithCorrectPositioning(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue = TodoListModel.add(this.todoListValue, "someNowTask")
      .pipe(todoList => TodoListModel.add(todoList, "someOtherNowTask"))
      .pipe(todoList => TodoListModel.addDeferred(todoList, "someLaterTask"))
      .pipe(todoList => TodoListModel.displace(todoList, "displace it"))
      .pipe(todoList => TodoListModel.unlock(todoList, now))
    assertThat(TodoListModel.getTodos(todoListValue).asJavaCollection).containsExactly(
      Todo("displace it"),
      Todo("someOtherNowTask"))
    assertThat(TodoListModel.getDeferredTodos(todoListValue, now).asJavaCollection).containsExactly(
      Todo("someNowTask"),
      Todo("someLaterTask"))
  }

  @Test
  def update_whenTodoWithIdentifierExists_updatesTodo(): Unit = {
    val todoListValue = TodoListModel.add(this.todoListValue, "someTask")
      .pipe(todoList => TodoListModel.update(todoList, 0, "someOtherTask"))
    val todo :: _ = TodoListModel.getTodos(todoListValue)
    Assertions.assertThat(todo.task).isEqualTo("someOtherTask")
  }

  @Test
  def complete_whenTodoWithIdentifierExists_completesTodo_placingItBeforeExistingCompletedTodos(): Unit = {
    assertThat(this.todoListValue.completedTodos.asJavaCollection).isEmpty()
    val completedAt = Date.from(Instant.now())
    val todoListValue = TodoListModel.add(this.todoListValue, "someTask")
      .pipe(todoList => TodoListModel.add(todoList, "someOtherTask"))
      .pipe(todoList => TodoListModel.complete(todoList, 0, completedAt))
      .pipe(todoList => TodoListModel.complete(todoList, 0, completedAt))
    assertThat(todoListValue.todos.asJavaCollection).isEmpty()
    assertThat(todoListValue.completedTodos.asJavaCollection).containsExactly(
      CompletedTodo("someTask", completedAt),
      CompletedTodo("someOtherTask", completedAt)
    )
  }

  @Test
  def move_whenTodoWithIdentifierExists_whenTargetExists_movesTodoDown(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue1 = TodoListModel.add(this.todoListValue, "now1")
      .pipe(todoList => TodoListModel.add(todoList, "now2"))
      .pipe(todoList => TodoListModel.unlock(todoList, now))
    val tasks = List("someTask", "anotherTask", "yetAnotherTask", "evenYetAnotherTask")
    val todoListValue2 = tasks.zipWithIndex.foldLeft(todoListValue1) { (todoList: TodoListModel, next) =>
      val (task, _) = next
      TodoListModel.addDeferred(todoList, task)
    }
    val todoListValue3 = TodoListModel.move(todoListValue2, 2, 4)
    assertThat(TodoListModel.getDeferredTodos(todoListValue3, now).asJavaCollection).extracting("task")
      .containsExactly("anotherTask", "yetAnotherTask", "someTask", "evenYetAnotherTask")
  }

  @Test
  def move_whenTodoWithIdentifierExists_whenTargetExists_movesTodoUp(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue1 = TodoListModel.add(this.todoListValue, "now1")
      .pipe(todoList => TodoListModel.add(todoList, "now2"))
      .pipe(todoList => TodoListModel.unlock(todoList, now))
    val tasks = List("someTask", "anotherTask", "yetAnotherTask", "evenYetAnotherTask")
    val todoListValue2 = tasks.zipWithIndex.foldLeft(todoListValue1) { (todoList: TodoListModel, next) =>
      val (task, _) = next
      TodoListModel.addDeferred(todoList, task)
    }
    val todoListValue3 = TodoListModel.move(todoListValue2, 5, 3)
    assertThat(TodoListModel.getDeferredTodos(todoListValue3, now).asJavaCollection).extracting("task")
      .containsExactly("someTask", "evenYetAnotherTask", "anotherTask", "yetAnotherTask")
  }

  @Test
  def move_beforeOrAfter_whenTodoWithIdentifierExists_whenTargetExists_whenOriginalAndTargetPositionsAreSame_doesNothing(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue1 = TodoListModel.add(this.todoListValue, "now1")
      .pipe(todoList => TodoListModel.add(todoList, "now2"))
      .pipe(todoList => TodoListModel.unlock(todoList, now))
    val tasks = List("someTask", "anotherTask", "yetAnotherTask", "evenYetAnotherTask")
    val todoListValue2 = tasks.zipWithIndex.foldLeft(todoListValue1) { (todoList: TodoListModel, next) =>
      val (task, _) = next
      TodoListModel.addDeferred(todoList, task)
    }
    val todoListValue3 = TodoListModel.move(todoListValue2, 2, 2)
    assertThat(TodoListModel.getDeferredTodos(todoListValue3, now).asJavaCollection).extracting("task").containsExactlyElementsOf(tasks.asJavaCollection)
  }

  @Test
  def pull_whenThereAreNoImmediateTodos_fillsFromPostponedList(): Unit = {
    val todoListValue = TodoListModel.addDeferred(this.todoListValue, "firstLater")
      .pipe(todoList => TodoListModel.addDeferred(todoList, "secondLater"))
      .pipe(todoList => TodoListModel.pull(todoList))
    assertThat(TodoListModel.getTodos(todoListValue).asJavaCollection).extracting("task").containsExactly("firstLater", "secondLater")
  }

  @Test
  def pull_whenThereAreNoImmediateTodos_andOneDeferredTodo_fillsFromPostponedList(): Unit = {
    val todoListValue = TodoListModel.addDeferred(this.todoListValue, "firstLater")
      .pipe(todoList => TodoListModel.pull(todoList))
    assertThat(TodoListModel.getTodos(todoListValue).asJavaCollection).extracting("task").containsExactly("firstLater")
  }

  @Test
  def pull_whenThereAreLessImmediateTodosThanMaxSize_fillsFromPostponedList_withAsManyTodosAsTheDifference(): Unit = {
    val todoListValue = TodoListModel.add(this.todoListValue, "firstNow")
      .pipe(todoList => TodoListModel.addDeferred(todoList, "firstLater"))
      .pipe(todoList => TodoListModel.addDeferred(todoList, "secondLater"))
      .pipe(todoList => TodoListModel.addDeferred(todoList, "thirdLater"))
      .pipe(todoList => TodoListModel.pull(todoList))
    assertThat(TodoListModel.getTodos(todoListValue).asJavaCollection).extracting("task").containsExactly("firstNow", "firstLater")
  }

  @Test
  def pull_whenThereAreAsManyImmediateTodosAsMaxSize_doesNotFillFromPostponedList(): Unit = {
    val todoListValue = TodoListModel.add(this.todoListValue, "someTask")
      .pipe(todoList => TodoListModel.add(todoList, "someOtherTask"))
      .pipe(todoList => TodoListModel.addDeferred(todoList, "firstLater"))
      .pipe(todoList => TodoListModel.pull(todoList))
    assertThat(TodoListModel.getTodos(todoListValue).asJavaCollection).extracting("task").containsExactly("someOtherTask", "someTask")
  }

  @Test
  def pull_whenThereIsASourceList_whenThereAreLessTodosThanMaxSize_whenSourceListIsEmpty_doesNotFillListFromSource(): Unit = {
    val todoListValue = TodoListModel.pull(this.todoListValue)
    assertThat(TodoListModel.getTodos(todoListValue).asJavaCollection).isEmpty()
  }

  @Test
  def escalate_swapsPositionsOfLastTodoAndFirstDeferredTodo(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue = TodoListModel.add(this.todoListValue, "will be deferred after escalate")
      .pipe(todoList => TodoListModel.add(todoList, "some task"))
      .pipe(todoList => TodoListModel.addDeferred(todoList, "will no longer be deferred after escalate"))
      .pipe(todoList => TodoListModel.escalate(todoList))
      .pipe(todoList => TodoListModel.unlock(todoList, now))
    assertThat(TodoListModel.getTodos(todoListValue).asJavaCollection).containsExactly(
      Todo("some task"),
      Todo("will no longer be deferred after escalate"))
    assertThat(TodoListModel.getDeferredTodos(todoListValue, now).asJavaCollection).containsExactly(
      Todo("will be deferred after escalate"))
  }

  @Test
  def unlockDuration_returnsRemainingDurationWhenStillUnlocked(): Unit = {
    val currentInstant = Instant.now()
    val todoList = TodoListModel.unlock(this.todoListValue, Date.from(currentInstant.minusSeconds(1799)))

    val duration = TodoListModel.unlockDurationMs(todoList, Date.from(currentInstant))

    assertThat(duration).isEqualTo(1000L)
  }

  @Test
  def unlockDuration_returnsZeroWhenLocked(): Unit = {
    val currentInstant = Instant.now()
    val todoList = TodoListModel.unlock(this.todoListValue, Date.from(currentInstant.minusSeconds(1801)))

    val duration = TodoListModel.unlockDurationMs(todoList, Date.from(currentInstant))

    assertThat(duration).isEqualTo(0L)
  }

  @Test
  def capabilities_thatAreAlwaysIncluded(): Unit = {
    val capabilities = TodoListModel.capabilities(this.todoListValue, Date.from(Instant.now()))

    assertThat(capabilities.addDeferred("someTask")).isEqualTo(DeferredTodoAddedEvent("someTask"))
  }

  @Test
  def capabilities_includeAdd_whenListHasCapacity(): Unit = {
    val capabilities = TodoListModel.capabilities(this.todoListValue, Date.from(Instant.now()))

    assertThat(capabilities.add.isDefined).isTrue
    assertThat(capabilities.add.get.apply("someTask")).isEqualTo(TodoAddedEvent("someTask"))
  }

  @Test
  def capabilities_doesNotIncludeAdd_whenListDoesNotHaveCapacity(): Unit = {
    val capabilities = TodoListModel.add(this.todoListValue, "task 1")
      .pipe(todoList => TodoListModel.add(todoList, "task 2"))
      .pipe(todoList => TodoListModel.capabilities(todoList, Date.from(Instant.now())))

    assertThat(capabilities.add.isEmpty).isTrue
  }

  @Test
  def capabilities_includeDisplace_whenListDoesNotHaveCapacity(): Unit = {
    val capabilities = TodoListModel.add(this.todoListValue, "task 1")
      .pipe(todoList => TodoListModel.add(todoList, "task 2"))
      .pipe(todoList => TodoListModel.capabilities(todoList, Date.from(Instant.now())))

    assertThat(capabilities.displace.isDefined).isTrue
    assertThat(capabilities.displace.get.apply("someTask")).isEqualTo(TodoDisplacedEvent("someTask"))
  }

  @Test
  def capabilities_doesNotIncludeDisplace_whenListHasCapacity(): Unit = {
    val capabilities = TodoListModel.capabilities(this.todoListValue, Date.from(Instant.now()))

    assertThat(capabilities.displace.isEmpty).isTrue
  }

  @Test
  def capabilities_includePull_whenListIsAbleToBeReplenished(): Unit = {
    val capabilities = TodoListModel.add(this.todoListValue, "task 1")
      .pipe(todoList => TodoListModel.addDeferred(todoList, "task 2"))
      .pipe(todoList => TodoListModel.capabilities(todoList, Date.from(Instant.now())))

    assertThat(capabilities.pull.isDefined).isTrue
    assertThat(capabilities.pull.get).isEqualTo(PulledEvent())
  }

  @Test
  def capabilities_doesNotIncludePull_whenListIsNotAbleToBeReplenished(): Unit = {
    val capabilities = TodoListModel.add(this.todoListValue, "task 1")
      .pipe(todoList => TodoListModel.add(todoList, "task 2"))
      .pipe(todoList => TodoListModel.addDeferred(todoList, "task 3"))
      .pipe(todoList => TodoListModel.capabilities(todoList, Date.from(Instant.now())))

    assertThat(capabilities.pull.isEmpty).isTrue
  }

  @Test
  def capabilities_includeEscalate_whenListIsAbleToBeEscalated(): Unit = {
    val capabilities = TodoListModel.add(this.todoListValue, "task 1")
      .pipe(todoList => TodoListModel.add(todoList, "task 2"))
      .pipe(todoList => TodoListModel.addDeferred(todoList, "task 3"))
      .pipe(todoList => TodoListModel.capabilities(todoList, Date.from(Instant.now())))

    assertThat(capabilities.escalate.isDefined).isTrue
    assertThat(capabilities.escalate.get).isEqualTo(EscalatedEvent())
  }

  @Test
  def capabilities_doesNotIncludeEscalate_whenListIsNotAbleToBeEscalated(): Unit = {
    val capabilities = TodoListModel.add(this.todoListValue, "task 1")
      .pipe(todoList => TodoListModel.add(todoList, "task 2"))
      .pipe(todoList => TodoListModel.capabilities(todoList, Date.from(Instant.now())))

    assertThat(capabilities.escalate.isEmpty).isTrue
  }

  @Test
  def capabilities_includeUnlock_whenListIsAbleToBeUnlocked(): Unit = {
    val capabilities = TodoListModel.capabilities(this.todoListValue, Date.from(Instant.now()))

    assertThat(capabilities.unlock.isDefined).isTrue
    val unlockDate = Date.from(Instant.now())
    assertThat(capabilities.unlock.get.apply(unlockDate)).isEqualTo(UnlockedEvent(unlockDate))
  }

  @Test
  def capabilities_doesNotIncludeUnlock_whenListIsNotAbleToBeUnlocked(): Unit = {
    val capabilities = TodoListModel.capabilities(this.todoListValue, Date.from(Instant.EPOCH.plus(23, ChronoUnit.HOURS)))

    assertThat(capabilities.unlock.isEmpty).isTrue
  }

  @Test
  def capabilites_includeCapabilitiesForEachTodo(): Unit = {
    val capabilities = TodoListModel.add(this.todoListValue, "someTask")
      .pipe(todoList => TodoListModel.add(todoList, "someOtherTask"))
      .pipe(todoList => TodoListModel.addDeferred(todoList, "someDeferredTask"))
      .pipe(todoList => TodoListModel.capabilities(todoList, Date.from(Instant.now())))

    assertThat(capabilities.todoCapabilities.size).isEqualTo(2)
    val todoCapabilities = capabilities.todoCapabilities.head
    assertThat(todoCapabilities.update("someUpdatedTask")).isEqualTo(TodoUpdatedEvent(0, "someUpdatedTask"))
    val completedAt = Date.from(Instant.now())
    assertThat(todoCapabilities.complete(completedAt)).isEqualTo(TodoCompletedEvent(0, completedAt))
    assertThat(todoCapabilities.delete).isEqualTo(TodoDeletedEvent(0))
    assertThat(todoCapabilities.move(1)).isEqualTo(TodoMovedEvent(0, 1))
  }

  @Test
  def capabilites_includeCapabilitiesForEachDeferredTodo(): Unit = {
    val capabilities = TodoListModel.addDeferred(this.todoListValue, "someTask")
      .pipe(todoList => TodoListModel.addDeferred(todoList, "someOtherTask"))
      .pipe(todoList => TodoListModel.add(todoList, "someNonDeferredTask"))
      .pipe(todoList => TodoListModel.add(todoList, "someOtherNonDeferredTask"))
      .pipe(todoList => TodoListModel.unlock(todoList, Date.from(Instant.now())))
      .pipe(todoList => TodoListModel.capabilities(todoList, Date.from(Instant.now())))

    assertThat(capabilities.todoCapabilities.size).isEqualTo(2)
    val todoCapabilities1 = capabilities.todoCapabilities.head
    assertThat(todoCapabilities1.move.head).isEqualTo(TodoMovedEvent(0, 0))
    assertThat(todoCapabilities1.move(1)).isEqualTo(TodoMovedEvent(0, 1))
    val todoCapabilities2 = capabilities.todoCapabilities.last
    assertThat(todoCapabilities2.move.head).isEqualTo(TodoMovedEvent(1, 0))
    assertThat(todoCapabilities2.move(1)).isEqualTo(TodoMovedEvent(1, 1))

    assertThat(capabilities.deferredTodoCapabilities.size).isEqualTo(2)
    val deferredTodoCapabilities1 = capabilities.deferredTodoCapabilities.head
    assertThat(deferredTodoCapabilities1.update("someUpdatedTask")).isEqualTo(TodoUpdatedEvent(2, "someUpdatedTask"))
    val completedAt = Date.from(Instant.now())
    assertThat(deferredTodoCapabilities1.complete(completedAt)).isEqualTo(TodoCompletedEvent(2, completedAt))
    assertThat(deferredTodoCapabilities1.delete).isEqualTo(TodoDeletedEvent(2))
    assertThat(deferredTodoCapabilities1.move.head).isEqualTo(TodoMovedEvent(2, 2))
    assertThat(deferredTodoCapabilities1.move(1)).isEqualTo(TodoMovedEvent(2, 3))
    val deferredTodoCapabilities2 = capabilities.deferredTodoCapabilities.last
    assertThat(deferredTodoCapabilities2.move.head).isEqualTo(TodoMovedEvent(3, 2))
    assertThat(deferredTodoCapabilities2.move(1)).isEqualTo(TodoMovedEvent(3, 3))
  }
}