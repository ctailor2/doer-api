package com.doerapispring.domain

import java.time.Instant
import java.util.Date

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

import scala.jdk.CollectionConverters._

//noinspection AccessorLikeMethodIsUnit
class TodoListModelTest {
  private val todoListValue: TodoListModel = new TodoListModel(scala.collection.immutable.List.empty, scala.collection.immutable.List.empty, Date.from(Instant.EPOCH), 0)

  @Test
  def add_addsToNowList(): Unit = {
    val todoListValue = TodoListModel.add(this.todoListValue, "someTask").get
    assertThat(TodoListModel.getTodos(todoListValue).asJavaCollection).containsExactly(Todo("someTask"))
  }

  @Test
  def add_addsToNowList_beforeFirstTodo(): Unit = {
    val todoListValue = TodoListModel.add(this.todoListValue, "someTask")
      .flatMap(todoList => TodoListModel.add(todoList, "someOtherTask"))
      .getOrElse(throw new RuntimeException)

    assertThat(TodoListModel.getTodos(todoListValue).asJavaCollection).extracting("task").containsExactly("someOtherTask", "someTask")
  }

  @Test
  def add_whenListAlreadyContainsTask_doesNotAdd_throwsDuplicateTodoException(): Unit = {
    val todoList = TodoListModel.add(this.todoListValue, "sameTask").get
    assertThat(TodoListModel.add(todoList, "sameTask").failed.get).isInstanceOf(classOf[DuplicateTodoException])
  }

  @Test
  def addDeferred_addsToLaterList(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue = TodoListModel.addDeferred(this.todoListValue, "someTask")
      .flatMap(todoList => TodoListModel.unlock(todoList, now))
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListModel.getDeferredTodos(todoListValue, now).asJavaCollection).containsExactly(Todo("someTask"))
  }

  @Test
  def addDeferred_addsToLaterList_afterLastTodo(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue = TodoListModel.addDeferred(this.todoListValue, "someTask")
      .flatMap(todoList => TodoListModel.addDeferred(todoList, "someOtherTask"))
      .flatMap(todoList => TodoListModel.unlock(todoList, now))
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListModel.getDeferredTodos(todoListValue, now).asJavaCollection).extracting("task").containsExactly("someTask", "someOtherTask")
  }

  @Test
  def addDeferred_whenListAlreadyContainsTask_doesNotAdd_throwsDuplicateTodoException(): Unit = {
    val todoList = TodoListModel.addDeferred(this.todoListValue, "sameTask").get
    assertThat(TodoListModel.addDeferred(todoList, "sameTask").failed.get).isInstanceOf(classOf[DuplicateTodoException])
  }

  @Test
  def delete_whenTodoWithIdentifierExists_removesTodo(): Unit = {
    val todoListValue = TodoListModel.add(this.todoListValue, "someTask")
      .flatMap(todoList => TodoListModel.delete(todoList, 0))
      .getOrElse(throw new RuntimeException)
    assertThat(todoListValue.todos.asJavaCollection).isEmpty()
    assertThat(TodoListModel.getTodos(todoListValue).asJavaCollection).isEmpty()
  }

  @Test
  def delete_whenTodoWithIdentifierExists_removesDeferredTodo(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue = TodoListModel.addDeferred(this.todoListValue, "someTask")
      .flatMap(todoList => TodoListModel.delete(todoList, 0))
      .flatMap(todoList => TodoListModel.unlock(todoList, now))
      .getOrElse(throw new RuntimeException)
    assertThat(todoListValue.todos.asJavaCollection).isEmpty()
    assertThat(TodoListModel.getDeferredTodos(todoListValue, now).asJavaCollection).isEmpty()
  }

  @Test
  def displace_whenListIsNotFull_throwsListNotFullException(): Unit = {
    assertThat(TodoListModel.displaceCapability(todoListValue).failed.get).isInstanceOf(classOf[ListNotFullException])
  }

  @Test
  def displace_whenListIsFull_whenTaskAlreadyExists_throwsDuplicateTodoException(): Unit = {
    val todoList = TodoListModel.add(this.todoListValue, "task")
      .flatMap(todoList => TodoListModel.add(todoList, "sameTask"))
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListModel.displace(todoList, "sameTask").failed.get).isInstanceOf(classOf[DuplicateTodoException])
  }

  @Test
  def displace_whenPostponedListIsEmpty_replacesTodo_andPushesItIntoPostponedListWithCorrectPositioning(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue = TodoListModel.add(this.todoListValue, "someNowTask")
      .flatMap(todoList => TodoListModel.add(todoList, "someOtherNowTask"))
      .flatMap(todoList => TodoListModel.displace(todoList, "displace it"))
      .flatMap(todoList => TodoListModel.unlock(todoList, now))
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListModel.getTodos(todoListValue).asJavaCollection).containsExactly(
      Todo("displace it"),
      Todo("someOtherNowTask"))
    assertThat(TodoListModel.getDeferredTodos(todoListValue, now).asJavaCollection).containsExactly(Todo("someNowTask"))
  }

  @Test
  def displace_whenPostponedIsNotEmpty_replacesTodo_andPushesItIntoPostponedListWithCorrectPositioning(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue = TodoListModel.add(this.todoListValue, "someNowTask")
      .flatMap(todoList => TodoListModel.add(todoList, "someOtherNowTask"))
      .flatMap(todoList => TodoListModel.addDeferred(todoList, "someLaterTask"))
      .flatMap(todoList => TodoListModel.displace(todoList, "displace it"))
      .flatMap(todoList => TodoListModel.unlock(todoList, now))
      .getOrElse(throw new RuntimeException)
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
      .flatMap(todoList => TodoListModel.update(todoList, 0, "someOtherTask"))
      .getOrElse(throw new RuntimeException)
    val todo :: _ = TodoListModel.getTodos(todoListValue)
    Assertions.assertThat(todo.task).isEqualTo("someOtherTask")
  }

  @Test
  def update_whenTaskAlreadyExists_throwsDuplicateTodoException(): Unit = {
    val todoList = TodoListModel.add(this.todoListValue, "sameTask").get

    assertThat(TodoListModel.update(todoList, 0, "sameTask").failed.get).isInstanceOf(classOf[DuplicateTodoException])
  }

  @Test
  def complete_whenTodoWithIdentifierExists_completesTodo_placingItBeforeExistingCompletedTodos(): Unit = {
    assertThat(this.todoListValue.completedTodos.asJavaCollection).isEmpty()
    val completedAt = Date.from(Instant.now())
    val todoListValue = TodoListModel.add(this.todoListValue, "someTask")
      .flatMap(todoList => TodoListModel.add(todoList, "someOtherTask"))
      .flatMap(todoList => TodoListModel.complete(todoList, 0, completedAt))
      .flatMap(todoList => TodoListModel.complete(todoList, 0, completedAt))
      .getOrElse(throw new RuntimeException)
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
      .flatMap(todoList => TodoListModel.add(todoList, "now2"))
      .flatMap(todoList => TodoListModel.unlock(todoList, now))
      .getOrElse(throw new RuntimeException)
    val tasks = List("someTask", "anotherTask", "yetAnotherTask", "evenYetAnotherTask")
    val todoListValue2 = tasks.zipWithIndex.foldLeft(todoListValue1) { (todoList: TodoListModel, next) =>
      val (task, _) = next
      TodoListModel.addDeferred(todoList, task).get
    }
    val todoListValue3 = TodoListModel.move(todoListValue2, 2, 4).get
    assertThat(TodoListModel.getDeferredTodos(todoListValue3, now).asJavaCollection).extracting("task")
      .containsExactly("anotherTask", "yetAnotherTask", "someTask", "evenYetAnotherTask")
  }

  @Test
  def move_whenTodoWithIdentifierExists_whenTargetExists_movesTodoUp(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue1 = TodoListModel.add(this.todoListValue, "now1")
      .flatMap(todoList => TodoListModel.add(todoList, "now2"))
      .flatMap(todoList => TodoListModel.unlock(todoList, now))
      .getOrElse(throw new RuntimeException)
    val tasks = List("someTask", "anotherTask", "yetAnotherTask", "evenYetAnotherTask")
    val todoListValue2 = tasks.zipWithIndex.foldLeft(todoListValue1) { (todoList: TodoListModel, next) =>
      val (task, _) = next
      TodoListModel.addDeferred(todoList, task).get
    }
    val todoListValue3 = TodoListModel.move(todoListValue2, 5, 3).get
    assertThat(TodoListModel.getDeferredTodos(todoListValue3, now).asJavaCollection).extracting("task")
      .containsExactly("someTask", "evenYetAnotherTask", "anotherTask", "yetAnotherTask")
  }

  @Test
  def move_beforeOrAfter_whenTodoWithIdentifierExists_whenTargetExists_whenOriginalAndTargetPositionsAreSame_doesNothing(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue1 = TodoListModel.add(this.todoListValue, "now1")
      .flatMap(todoList => TodoListModel.add(todoList, "now2"))
      .flatMap(todoList => TodoListModel.unlock(todoList, now))
      .getOrElse(throw new RuntimeException)
    val tasks = List("someTask", "anotherTask", "yetAnotherTask", "evenYetAnotherTask")
    val todoListValue2 = tasks.zipWithIndex.foldLeft(todoListValue1) { (todoList: TodoListModel, next) =>
      val (task, _) = next
      TodoListModel.addDeferred(todoList, task).get
    }
    val todoListValue3 = TodoListModel.move(todoListValue2, 2, 2).get
    assertThat(TodoListModel.getDeferredTodos(todoListValue3, now).asJavaCollection).extracting("task").containsExactlyElementsOf(tasks.asJavaCollection)
  }

  @Test
  @throws[Exception]
  def pull_whenThereAreNoImmediateTodos_fillsFromPostponedList(): Unit = {
    val todoListValue = TodoListModel.addDeferred(this.todoListValue, "firstLater")
      .flatMap(todoList => TodoListModel.addDeferred(todoList, "secondLater"))
      .flatMap(todoList => TodoListModel.pull(todoList))
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListModel.getTodos(todoListValue).asJavaCollection).extracting("task").containsExactly("firstLater", "secondLater")
  }

  @Test
  @throws[Exception]
  def pull_whenThereAreNoImmediateTodos_andOneDeferredTodo_fillsFromPostponedList(): Unit = {
    val todoListValue = TodoListModel.addDeferred(this.todoListValue, "firstLater")
      .flatMap(todoList => TodoListModel.pull(todoList))
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListModel.getTodos(todoListValue).asJavaCollection).extracting("task").containsExactly("firstLater")
  }

  @Test
  @throws[Exception]
  def pull_whenThereAreLessImmediateTodosThanMaxSize_fillsFromPostponedList_withAsManyTodosAsTheDifference(): Unit = {
    val todoListValue = TodoListModel.add(this.todoListValue, "firstNow")
      .flatMap(todoList => TodoListModel.addDeferred(todoList, "firstLater"))
      .flatMap(todoList => TodoListModel.addDeferred(todoList, "secondLater"))
      .flatMap(todoList => TodoListModel.addDeferred(todoList, "thirdLater"))
      .flatMap(todoList => TodoListModel.pull(todoList))
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListModel.getTodos(todoListValue).asJavaCollection).extracting("task").containsExactly("firstNow", "firstLater")
  }

  @Test
  @throws[Exception]
  def pull_whenThereAreAsManyImmediateTodosAsMaxSize_doesNotFillFromPostponedList(): Unit = {
    val todoListValue = TodoListModel.add(this.todoListValue, "someTask")
      .flatMap(todoList => TodoListModel.add(todoList, "someOtherTask"))
      .flatMap(todoList => TodoListModel.addDeferred(todoList, "firstLater"))
      .flatMap(todoList => TodoListModel.pull(todoList))
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListModel.getTodos(todoListValue).asJavaCollection).extracting("task").containsExactly("someOtherTask", "someTask")
  }

  @Test
  def pull_whenThereIsASourceList_whenThereAreLessTodosThanMaxSize_whenSourceListIsEmpty_doesNotFillListFromSource(): Unit = {
    val todoListValue = TodoListModel.pull(this.todoListValue).get
    assertThat(TodoListModel.getTodos(todoListValue).asJavaCollection).isEmpty()
  }

  @Test
  @throws[Exception]
  def escalate_swapsPositionsOfLastTodoAndFirstDeferredTodo(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue = TodoListModel.add(this.todoListValue, "will be deferred after escalate")
      .flatMap(todoList => TodoListModel.add(todoList, "some task"))
      .flatMap(todoList => TodoListModel.addDeferred(todoList, "will no longer be deferred after escalate"))
      .flatMap(todoList => TodoListModel.escalate(todoList))
      .flatMap(todoList => TodoListModel.unlock(todoList, now))
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListModel.getTodos(todoListValue).asJavaCollection).containsExactly(
      Todo("some task"),
      Todo("will no longer be deferred after escalate"))
    assertThat(TodoListModel.getDeferredTodos(todoListValue, now).asJavaCollection).containsExactly(
      Todo("will be deferred after escalate"))
  }

  @Test def isAbleToBeUnlocked_whenThereAreNoListUnlocks_returnsTrue(): Unit = {
    assertThat(TodoListModel.unlockCapability(this.todoListValue, Date.from(Instant.now())).isSuccess).isTrue
  }

  @Test
  @throws[Exception]
  def isAbleToBeUnlocked_whenThereAreListUnlocks_whenFirstListUnlockWasCreatedToday_returnsFalse(): Unit = {
    val todoListValue = this.todoListValue.copy(lastUnlockedAt = Date.from(Instant.ofEpochMilli(618537600000L))) // Tuesday, August 8, 1989 12:00:00 AM

    val instant = Instant.ofEpochMilli(618623999999L) // Tuesday, August 8, 1989 11:59:59 PM
    assertThat(TodoListModel.unlockCapability(todoListValue, Date.from(instant)).isSuccess).isFalse
  }

  @Test
  @throws[Exception]
  def isAbleToBeUnlocked_whenThereAreListUnlocks_whenFirstListUnlockWasCreatedBeforeToday_returnsTrue(): Unit = {
    val todoListValue = this.todoListValue.copy(lastUnlockedAt = Date.from(Instant.ofEpochMilli(618429599999L))) // Monday, August 7, 1989 11:29:59 PM

    val instant = Instant.ofEpochMilli(618537600000L) // Tuesday, August 8, 1989 12:00:00 AM
    assertThat(TodoListModel.unlockCapability(todoListValue, Date.from(instant)).isSuccess).isTrue
  }

  @Test
  @throws[Exception]
  def isAbleToBeUnlocked_whenThereAreListUnlocks_whenFirstListUnlockWasCreatedBeforeToday_butListIsStillUnlocked_returnsFalse(): Unit = {
    val todoListValue = this.todoListValue.copy(lastUnlockedAt = Date.from(Instant.ofEpochMilli(618536700000L))) // Monday, August 7, 1989 11:45:00 PM

    val instant = Instant.ofEpochMilli(618537900000L) // Tuesday, August 8, 1989 12:05:00 AM
    assertThat(TodoListModel.unlockCapability(todoListValue, Date.from(instant)).isSuccess).isFalse
  }

  @Test
  @throws[Exception]
  def pullCapability_whenThereAreDeferredTodos_andTheListIsNotFull_isSuccess(): Unit = {
    val todoListValue = TodoListModel.addDeferred(this.todoListValue, "someTask").get
    assertThat(TodoListModel.pullCapability(todoListValue).isSuccess).isTrue
  }

  @Test
  @throws[Exception]
  def pullCapability_whenThereAreDeferredTodos_andTheListIsFull_isNotSuccess(): Unit = {
    val todoListValue = TodoListModel.add(this.todoListValue, "todo1")
      .flatMap(todoList => TodoListModel.add(todoList, "todo2"))
      .flatMap(todoList => TodoListModel.addDeferred(todoList, "someTask"))
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListModel.pullCapability(todoListValue).isSuccess).isFalse
  }

  @Test
  @throws[Exception]
  def pullCapability_whenThereAreNoDeferredTodos_andTheListIsNotFull_isNotSuccess(): Unit = {
    val todoListValue = TodoListModel.add(this.todoListValue, "todo1").get
    assertThat(TodoListModel.pullCapability(todoListValue).isSuccess).isFalse
  }

  @Test
  @throws[Exception]
  def escalateCapability_whenTheListIsFull_andThereAreDeferredTodos_isSuccess(): Unit = {
    val todoListValue = TodoListModel.add(this.todoListValue, "task 1")
      .flatMap(todoList => TodoListModel.add(todoList, "task 2"))
      .flatMap(todoList => TodoListModel.addDeferred(todoList, "task 3"))
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListModel.escalateCapability(todoListValue).isSuccess).isTrue
  }

  @Test
  @throws[Exception]
  def escalateCapability_whenTheListIsNotFull_andThereAreDeferredTodos_isNotSuccess(): Unit = {
    val todoListValue = TodoListModel.add(this.todoListValue, "task 1")
      .flatMap(todoList => TodoListModel.addDeferred(todoList, "task 2"))
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListModel.escalateCapability(todoListValue).isSuccess).isFalse
  }

  @Test
  @throws[Exception]
  def escalateCapability_whenTheListIsFull_andThereAreNoDeferredTodos_isNotSuccess(): Unit = {
    val todoListValue = TodoListModel.add(this.todoListValue, "task 1")
      .flatMap(todoList => TodoListModel.add(todoList, "task 2"))
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListModel.escalateCapability(todoListValue).isSuccess).isFalse
  }

  @Test
  def unlockDuration_returnsRemainingDurationWhenStillUnlocked(): Unit = {
    val currentInstant = Instant.now()
    val todoList = TodoListModel.unlock(this.todoListValue, Date.from(currentInstant.minusSeconds(1799)))
      .getOrElse(throw new RuntimeException)

    val duration = TodoListModel.unlockDurationMs(todoList, Date.from(currentInstant))

    assertThat(duration).isEqualTo(1000L)
  }

  @Test
  def unlockDuration_returnsZeroWhenLocked(): Unit = {
    val currentInstant = Instant.now()
    val todoList = TodoListModel.unlock(this.todoListValue, Date.from(currentInstant.minusSeconds(1801)))
      .getOrElse(throw new RuntimeException)

    val duration = TodoListModel.unlockDurationMs(todoList, Date.from(currentInstant))

    assertThat(duration).isEqualTo(0L)
  }
}