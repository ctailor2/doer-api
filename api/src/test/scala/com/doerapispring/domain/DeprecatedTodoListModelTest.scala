package com.doerapispring.domain

import java.time.Instant
import java.util.Date

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

import scala.jdk.CollectionConverters._

//noinspection AccessorLikeMethodIsUnit
class DeprecatedTodoListModelTest {
  private val todoListValue: DeprecatedTodoListModel = new DeprecatedTodoListModel(new ListId("someListId"), "someProfileName", scala.collection.immutable.List.empty, Date.from(Instant.EPOCH), 0)

  @Test
  def add_addsToNowList(): Unit = {
    val todoListValue = DeprecatedTodoListModel.add(this.todoListValue, new TodoId("someId"), "someTask").get
    assertThat(DeprecatedTodoListModel.getTodos(todoListValue).asJavaCollection).containsExactly(new DeprecatedTodo(new TodoId("someId"), "someTask"))
  }

  @Test
  def add_addsToNowList_beforeFirstTodo(): Unit = {
    val todoListValue = DeprecatedTodoListModel.add(this.todoListValue, new TodoId("someId"), "someTask")
      .flatMap(todoList => DeprecatedTodoListModel.add(todoList, new TodoId("someId"), "someOtherTask"))
      .getOrElse(throw new RuntimeException)

    assertThat(DeprecatedTodoListModel.getTodos(todoListValue).asJavaCollection).extracting("task").containsExactly("someOtherTask", "someTask")
  }

  @Test
  def add_whenListAlreadyContainsTask_doesNotAdd_throwsDuplicateTodoException(): Unit = {
    val todoList = DeprecatedTodoListModel.add(this.todoListValue, new TodoId("someId"), "sameTask").get
    assertThat(DeprecatedTodoListModel.add(todoList, new TodoId("someId"), "sameTask").failed.get).isInstanceOf(classOf[DuplicateTodoException])
  }

  @Test
  def addDeferred_addsToLaterList(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue = DeprecatedTodoListModel.addDeferred(this.todoListValue, new TodoId("someId"), "someTask")
      .flatMap(todoList => DeprecatedTodoListModel.unlock(todoList, now))
      .getOrElse(throw new RuntimeException)
    assertThat(DeprecatedTodoListModel.getDeferredTodos(todoListValue, now).asJavaCollection).containsExactly(new DeprecatedTodo(new TodoId("someId"), "someTask"))
  }

  @Test
  def addDeferred_addsToLaterList_afterLastTodo(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue = DeprecatedTodoListModel.addDeferred(this.todoListValue, new TodoId("someId"), "someTask")
      .flatMap(todoList => DeprecatedTodoListModel.addDeferred(todoList, new TodoId("someId"), "someOtherTask"))
      .flatMap(todoList => DeprecatedTodoListModel.unlock(todoList, now))
      .getOrElse(throw new RuntimeException)
    assertThat(DeprecatedTodoListModel.getDeferredTodos(todoListValue, now).asJavaCollection).extracting("task").containsExactly("someTask", "someOtherTask")
  }

  @Test
  def addDeferred_whenListAlreadyContainsTask_doesNotAdd_throwsDuplicateTodoException(): Unit = {
    val todoList = DeprecatedTodoListModel.addDeferred(this.todoListValue, new TodoId("someId"), "sameTask").get
    assertThat(DeprecatedTodoListModel.addDeferred(todoList, new TodoId("someId"), "sameTask").failed.get).isInstanceOf(classOf[DuplicateTodoException])
  }

  @Test
  def delete_whenTodoWithIdentifierExists_removesTodo(): Unit = {
    val todoId = new TodoId("someId")
    val todoListValue = DeprecatedTodoListModel.add(this.todoListValue, todoId, "someTask")
      .flatMap(todoList => DeprecatedTodoListModel.delete(todoList, todoId))
      .getOrElse(throw new RuntimeException)
    assertThat(DeprecatedTodoListModel.getTodos(todoListValue).asJavaCollection).isEmpty()
  }

  @Test
  def delete_whenTodoWithIdentifierExists_removesDeferredTodo(): Unit = {
    val now = Date.from(Instant.now())
    val todoId = new TodoId("someId")
    val todoListValue = DeprecatedTodoListModel.add(this.todoListValue, todoId, "someTask")
      .flatMap(todoList => DeprecatedTodoListModel.delete(todoList, todoId))
      .flatMap(todoList => DeprecatedTodoListModel.unlock(todoList, now))
      .getOrElse(throw new RuntimeException)
    assertThat(DeprecatedTodoListModel.getDeferredTodos(todoListValue, now).asJavaCollection).isEmpty()
  }

  @Test
  def delete_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException(): Unit = {
    assertThat(DeprecatedTodoListModel.delete(todoListValue, new TodoId("someBogusIdentifier")).failed.get).isInstanceOf(classOf[TodoNotFoundException])
  }

  @Test
  def displace_whenListIsNotFull_throwsListNotFullException(): Unit = {
    assertThat(DeprecatedTodoListModel.displaceCapability(todoListValue).failed.get).isInstanceOf(classOf[ListNotFullException])
  }

  @Test
  def displace_whenListIsFull_whenTaskAlreadyExists_throwsDuplicateTodoException(): Unit = {
    val todoList = DeprecatedTodoListModel.add(this.todoListValue, new TodoId("someId"), "task")
      .flatMap(todoList => DeprecatedTodoListModel.add(todoList, new TodoId("someId"), "sameTask"))
      .getOrElse(throw new RuntimeException)
    assertThat(DeprecatedTodoListModel.displace(todoList, new TodoId("someId"), "sameTask").failed.get).isInstanceOf(classOf[DuplicateTodoException])
  }

  @Test
  def displace_whenPostponedListIsEmpty_replacesTodo_andPushesItIntoPostponedListWithCorrectPositioning(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue = DeprecatedTodoListModel.add(this.todoListValue, new TodoId("1"), "someNowTask")
      .flatMap(todoList => DeprecatedTodoListModel.add(todoList, new TodoId("2"), "someOtherNowTask"))
      .flatMap(todoList => DeprecatedTodoListModel.displace(todoList, new TodoId("3"), "displace it"))
      .flatMap(todoList => DeprecatedTodoListModel.unlock(todoList, now))
      .getOrElse(throw new RuntimeException)
    assertThat(DeprecatedTodoListModel.getTodos(todoListValue).asJavaCollection).containsExactly(
      new DeprecatedTodo(new TodoId("3"), "displace it"),
      new DeprecatedTodo(new TodoId("2"), "someOtherNowTask"))
    assertThat(DeprecatedTodoListModel.getDeferredTodos(todoListValue, now).asJavaCollection).containsExactly(new DeprecatedTodo(new TodoId("1"), "someNowTask"))
  }

  @Test
  def displace_whenPostponedIsNotEmpty_replacesTodo_andPushesItIntoPostponedListWithCorrectPositioning(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue = DeprecatedTodoListModel.add(this.todoListValue, new TodoId("1"), "someNowTask")
      .flatMap(todoList => DeprecatedTodoListModel.add(todoList, new TodoId("2"), "someOtherNowTask"))
      .flatMap(todoList => DeprecatedTodoListModel.addDeferred(todoList, new TodoId("3"), "someLaterTask"))
      .flatMap(todoList => DeprecatedTodoListModel.displace(todoList, new TodoId("4"), "displace it"))
      .flatMap(todoList => DeprecatedTodoListModel.unlock(todoList, now))
      .getOrElse(throw new RuntimeException)
    assertThat(DeprecatedTodoListModel.getTodos(todoListValue).asJavaCollection).containsExactly(
      new DeprecatedTodo(new TodoId("4"), "displace it"),
      new DeprecatedTodo(new TodoId("2"), "someOtherNowTask"))
    assertThat(DeprecatedTodoListModel.getDeferredTodos(todoListValue, now).asJavaCollection).containsExactly(
      new DeprecatedTodo(new TodoId("1"), "someNowTask"),
      new DeprecatedTodo(new TodoId("3"), "someLaterTask"))
  }

  @Test
  def update_whenTodoWithIdentifierExists_updatesTodo(): Unit = {
    val todoId = new TodoId("someId")
    val todoListValue = DeprecatedTodoListModel.add(this.todoListValue, todoId, "someTask")
      .flatMap(todoList => DeprecatedTodoListModel.update(todoList, todoId, "someOtherTask"))
      .getOrElse(throw new RuntimeException)
    val todo :: _ = DeprecatedTodoListModel.getTodos(todoListValue)
    Assertions.assertThat(todo.getTask).isEqualTo("someOtherTask")
  }

  @Test
  def update_whenTaskAlreadyExists_throwsDuplicateTodoException(): Unit = {
    val todoId = new TodoId("someId")
    val todoList = DeprecatedTodoListModel.add(this.todoListValue, todoId, "sameTask").get

    assertThat(DeprecatedTodoListModel.update(todoList, todoId, "sameTask").failed.get).isInstanceOf(classOf[DuplicateTodoException])
  }

  @Test
  def update_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException(): Unit = {
    assertThat(DeprecatedTodoListModel.update(todoListValue, new TodoId("bananaPudding"), "sameTask").failed.get).isInstanceOf(classOf[TodoNotFoundException])
  }

  @Test
  def complete_whenTodoWithIdentifierExists_removesTodoFromMatchingList_returnsCompletedTodo(): Unit = {
    val todoId = new TodoId("someId")
    val todoListValue = DeprecatedTodoListModel.add(this.todoListValue, todoId, "someTask")
      .flatMap(todoList => DeprecatedTodoListModel.complete(todoList, todoId))
      .getOrElse(throw new RuntimeException)
    assertThat(DeprecatedTodoListModel.getTodos(todoListValue).asJavaCollection).isEmpty()
  }

  @Test
  def complete_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException(): Unit = {
    assertThat(DeprecatedTodoListModel.complete(todoListValue, new TodoId("someBogusIdentifier")).failed.get).isInstanceOf(classOf[TodoNotFoundException])
  }

  @Test
  def move_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException(): Unit = {
    assertThat(DeprecatedTodoListModel.move(todoListValue, new TodoId("junk"), new TodoId("bogus")).failed.get).isInstanceOf(classOf[TodoNotFoundException])
  }

  @Test
  def move_whenTodoWithIdentifierExists_whenTargetExists_movesTodoDown(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue1 = DeprecatedTodoListModel.add(this.todoListValue, new TodoId("someId"), "now1")
      .flatMap(todoList => DeprecatedTodoListModel.add(todoList, new TodoId("someId"), "now2"))
      .flatMap(todoList => DeprecatedTodoListModel.unlock(todoList, now))
      .getOrElse(throw new RuntimeException)
    val tasks = List("someTask", "anotherTask", "yetAnotherTask", "evenYetAnotherTask")
    val todoListValue2 = tasks.zipWithIndex.foldLeft(todoListValue1) { (todoList: DeprecatedTodoListModel, next) =>
      val (task, i) = next
      DeprecatedTodoListModel.addDeferred(todoList, new TodoId(String.valueOf(i)), task).get
    }
    val todoListValue3 = DeprecatedTodoListModel.move(todoListValue2, new TodoId("0"), new TodoId("2")).get
    assertThat(DeprecatedTodoListModel.getDeferredTodos(todoListValue3, now).asJavaCollection).extracting("task")
      .containsExactly("anotherTask", "yetAnotherTask", "someTask", "evenYetAnotherTask")
  }

  @Test
  def move_whenTodoWithIdentifierExists_whenTargetExists_movesTodoUp(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue1 = DeprecatedTodoListModel.add(this.todoListValue, new TodoId("someId"), "now1")
      .flatMap(todoList => DeprecatedTodoListModel.add(todoList, new TodoId("someId"), "now2"))
      .flatMap(todoList => DeprecatedTodoListModel.unlock(todoList, now))
      .getOrElse(throw new RuntimeException)
    val tasks = List("someTask", "anotherTask", "yetAnotherTask", "evenYetAnotherTask")
    val todoListValue2 = tasks.zipWithIndex.foldLeft(todoListValue1) { (todoList: DeprecatedTodoListModel, next) =>
      val (task, i) = next
      DeprecatedTodoListModel.addDeferred(todoList, new TodoId(String.valueOf(i)), task).get
    }
    val todoListValue3 = DeprecatedTodoListModel.move(todoListValue2, new TodoId("3"), new TodoId("1")).get
    assertThat(DeprecatedTodoListModel.getDeferredTodos(todoListValue3, now).asJavaCollection).extracting("task")
      .containsExactly("someTask", "evenYetAnotherTask", "anotherTask", "yetAnotherTask")
  }

  @Test
  def move_beforeOrAfter_whenTodoWithIdentifierExists_whenTargetExists_whenOriginalAndTargetPositionsAreSame_doesNothing(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue1 = DeprecatedTodoListModel.add(this.todoListValue, new TodoId("someId"), "now1")
      .flatMap(todoList => DeprecatedTodoListModel.add(todoList, new TodoId("someId"), "now2"))
      .flatMap(todoList => DeprecatedTodoListModel.unlock(todoList, now))
      .getOrElse(throw new RuntimeException)
    val tasks = List("someTask", "anotherTask", "yetAnotherTask", "evenYetAnotherTask")
    val todoListValue2 = tasks.zipWithIndex.foldLeft(todoListValue1) { (todoList: DeprecatedTodoListModel, next) =>
      val (task, i) = next
      DeprecatedTodoListModel.addDeferred(todoList, new TodoId(String.valueOf(i)), task).get
    }
    val todoListValue3 = DeprecatedTodoListModel.move(todoListValue2, new TodoId("0"), new TodoId("0")).get
    assertThat(DeprecatedTodoListModel.getDeferredTodos(todoListValue3, now).asJavaCollection).extracting("task").containsExactlyElementsOf(tasks.asJavaCollection)
  }

  @Test
  @throws[Exception]
  def pull_whenThereAreNoImmediateTodos_fillsFromPostponedList(): Unit = {
    val todoListValue = DeprecatedTodoListModel.addDeferred(this.todoListValue, new TodoId("someId"), "firstLater")
      .flatMap(todoList => DeprecatedTodoListModel.addDeferred(todoList, new TodoId("someId"), "secondLater"))
      .flatMap(todoList => DeprecatedTodoListModel.pull(todoList))
      .getOrElse(throw new RuntimeException)
    assertThat(DeprecatedTodoListModel.getTodos(todoListValue).asJavaCollection).extracting("task").containsExactly("firstLater", "secondLater")
  }

  @Test
  @throws[Exception]
  def pull_whenThereAreNoImmediateTodos_andOneDeferredTodo_fillsFromPostponedList(): Unit = {
    val todoListValue = DeprecatedTodoListModel.addDeferred(this.todoListValue, new TodoId("someId"), "firstLater")
      .flatMap(todoList => DeprecatedTodoListModel.pull(todoList))
      .getOrElse(throw new RuntimeException)
    assertThat(DeprecatedTodoListModel.getTodos(todoListValue).asJavaCollection).extracting("task").containsExactly("firstLater")
  }

  @Test
  @throws[Exception]
  def pull_whenThereAreLessImmediateTodosThanMaxSize_fillsFromPostponedList_withAsManyTodosAsTheDifference(): Unit = {
    val todoListValue = DeprecatedTodoListModel.add(this.todoListValue, new TodoId("someId"), "firstNow")
      .flatMap(todoList => DeprecatedTodoListModel.addDeferred(todoList, new TodoId("someId"), "firstLater"))
      .flatMap(todoList => DeprecatedTodoListModel.addDeferred(todoList, new TodoId("someId"), "secondLater"))
      .flatMap(todoList => DeprecatedTodoListModel.addDeferred(todoList, new TodoId("someId"), "thirdLater"))
      .flatMap(todoList => DeprecatedTodoListModel.pull(todoList))
      .getOrElse(throw new RuntimeException)
    assertThat(DeprecatedTodoListModel.getTodos(todoListValue).asJavaCollection).extracting("task").containsExactly("firstNow", "firstLater")
  }

  @Test
  @throws[Exception]
  def pull_whenThereAreAsManyImmediateTodosAsMaxSize_doesNotFillFromPostponedList(): Unit = {
    val todoListValue = DeprecatedTodoListModel.add(this.todoListValue, new TodoId("someId"), "someTask")
      .flatMap(todoList => DeprecatedTodoListModel.add(todoList, new TodoId("someId"), "someOtherTask"))
      .flatMap(todoList => DeprecatedTodoListModel.addDeferred(todoList, new TodoId("someId"), "firstLater"))
      .flatMap(todoList => DeprecatedTodoListModel.pull(todoList))
      .getOrElse(throw new RuntimeException)
    assertThat(DeprecatedTodoListModel.getTodos(todoListValue).asJavaCollection).extracting("task").containsExactly("someOtherTask", "someTask")
  }

  @Test
  def pull_whenThereIsASourceList_whenThereAreLessTodosThanMaxSize_whenSourceListIsEmpty_doesNotFillListFromSource(): Unit = {
    val todoListValue = DeprecatedTodoListModel.pull(this.todoListValue).get
    assertThat(DeprecatedTodoListModel.getTodos(todoListValue).asJavaCollection).isEmpty()
  }

  @Test
  @throws[Exception]
  def escalate_swapsPositionsOfLastTodoAndFirstDeferredTodo(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue = DeprecatedTodoListModel.add(this.todoListValue, new TodoId("someId"), "will be deferred after escalate")
      .flatMap(todoList => DeprecatedTodoListModel.add(todoList, new TodoId("someId"), "some task"))
      .flatMap(todoList => DeprecatedTodoListModel.addDeferred(todoList, new TodoId("someId"), "will no longer be deferred after escalate"))
      .flatMap(todoList => DeprecatedTodoListModel.escalate(todoList))
      .flatMap(todoList => DeprecatedTodoListModel.unlock(todoList, now))
      .getOrElse(throw new RuntimeException)
    assertThat(DeprecatedTodoListModel.getTodos(todoListValue).asJavaCollection).containsExactly(
      new DeprecatedTodo(new TodoId("someId"), "some task"),
      new DeprecatedTodo(new TodoId("someId"), "will no longer be deferred after escalate"))
    assertThat(DeprecatedTodoListModel.getDeferredTodos(todoListValue, now).asJavaCollection).containsExactly(
      new DeprecatedTodo(new TodoId("someId"), "will be deferred after escalate"))
  }

  @Test def isAbleToBeUnlocked_whenThereAreNoListUnlocks_returnsTrue(): Unit = {
    assertThat(DeprecatedTodoListModel.unlockCapability(this.todoListValue, Date.from(Instant.now())).isSuccess).isTrue
  }

  @Test
  @throws[Exception]
  def isAbleToBeUnlocked_whenThereAreListUnlocks_whenFirstListUnlockWasCreatedToday_returnsFalse(): Unit = {
    val todoListValue = this.todoListValue.copy(lastUnlockedAt = Date.from(Instant.ofEpochMilli(618537600000L))) // Tuesday, August 8, 1989 12:00:00 AM

    val instant = Instant.ofEpochMilli(618623999999L) // Tuesday, August 8, 1989 11:59:59 PM
    assertThat(DeprecatedTodoListModel.unlockCapability(todoListValue, Date.from(instant)).isSuccess).isFalse
  }

  @Test
  @throws[Exception]
  def isAbleToBeUnlocked_whenThereAreListUnlocks_whenFirstListUnlockWasCreatedBeforeToday_returnsTrue(): Unit = {
    val todoListValue = this.todoListValue.copy(lastUnlockedAt = Date.from(Instant.ofEpochMilli(618429599999L))) // Monday, August 7, 1989 11:29:59 PM

    val instant = Instant.ofEpochMilli(618537600000L) // Tuesday, August 8, 1989 12:00:00 AM
    assertThat(DeprecatedTodoListModel.unlockCapability(todoListValue, Date.from(instant)).isSuccess).isTrue
  }

  @Test
  @throws[Exception]
  def isAbleToBeUnlocked_whenThereAreListUnlocks_whenFirstListUnlockWasCreatedBeforeToday_butListIsStillUnlocked_returnsFalse(): Unit = {
    val todoListValue = this.todoListValue.copy(lastUnlockedAt = Date.from(Instant.ofEpochMilli(618536700000L))) // Monday, August 7, 1989 11:45:00 PM

    val instant = Instant.ofEpochMilli(618537900000L) // Tuesday, August 8, 1989 12:05:00 AM
    assertThat(DeprecatedTodoListModel.unlockCapability(todoListValue, Date.from(instant)).isSuccess).isFalse
  }

  @Test
  @throws[Exception]
  def pullCapability_whenThereAreDeferredTodos_andTheListIsNotFull_isSuccess(): Unit = {
    val todoListValue = DeprecatedTodoListModel.addDeferred(this.todoListValue, new TodoId("someId"), "someTask").get
    assertThat(DeprecatedTodoListModel.pullCapability(todoListValue).isSuccess).isTrue
  }

  @Test
  @throws[Exception]
  def pullCapability_whenThereAreDeferredTodos_andTheListIsFull_isNotSuccess(): Unit = {
    val todoListValue = DeprecatedTodoListModel.add(this.todoListValue, new TodoId("someId"), "todo1")
      .flatMap(todoList => DeprecatedTodoListModel.add(todoList, new TodoId("someId"), "todo2"))
      .flatMap(todoList => DeprecatedTodoListModel.addDeferred(todoList, new TodoId("someId"), "someTask"))
      .getOrElse(throw new RuntimeException)
    assertThat(DeprecatedTodoListModel.pullCapability(todoListValue).isSuccess).isFalse
  }

  @Test
  @throws[Exception]
  def pullCapability_whenThereAreNoDeferredTodos_andTheListIsNotFull_isNotSuccess(): Unit = {
    val todoListValue = DeprecatedTodoListModel.add(this.todoListValue, new TodoId("someId"), "todo1").get
    assertThat(DeprecatedTodoListModel.pullCapability(todoListValue).isSuccess).isFalse
  }

  @Test
  @throws[Exception]
  def escalateCapability_whenTheListIsFull_andThereAreDeferredTodos_isSuccess(): Unit = {
    val todoListValue = DeprecatedTodoListModel.add(this.todoListValue, new TodoId("someId"), "task 1")
      .flatMap(todoList => DeprecatedTodoListModel.add(todoList, new TodoId("someId"), "task 2"))
      .flatMap(todoList => DeprecatedTodoListModel.addDeferred(todoList, new TodoId("someId"), "task 3"))
      .getOrElse(throw new RuntimeException)
    assertThat(DeprecatedTodoListModel.escalateCapability(todoListValue).isSuccess).isTrue
  }

  @Test
  @throws[Exception]
  def escalateCapability_whenTheListIsNotFull_andThereAreDeferredTodos_isNotSuccess(): Unit = {
    val todoListValue = DeprecatedTodoListModel.add(this.todoListValue, new TodoId("someId"), "task 1")
      .flatMap(todoList => DeprecatedTodoListModel.addDeferred(todoList, new TodoId("someId"), "task 2"))
      .getOrElse(throw new RuntimeException)
    assertThat(DeprecatedTodoListModel.escalateCapability(todoListValue).isSuccess).isFalse
  }

  @Test
  @throws[Exception]
  def escalateCapability_whenTheListIsFull_andThereAreNoDeferredTodos_isNotSuccess(): Unit = {
    val todoListValue = DeprecatedTodoListModel.add(this.todoListValue, new TodoId("someId"), "task 1")
      .flatMap(todoList => DeprecatedTodoListModel.add(todoList, new TodoId("someId"), "task 2"))
      .getOrElse(throw new RuntimeException)
    assertThat(DeprecatedTodoListModel.escalateCapability(todoListValue).isSuccess).isFalse
  }

  @Test
  def unlockDuration_returnsRemainingDurationWhenStillUnlocked(): Unit = {
    val currentInstant = Instant.now()
    val todoList = DeprecatedTodoListModel.unlock(this.todoListValue, Date.from(currentInstant.minusSeconds(1799)))
      .getOrElse(throw new RuntimeException)

    val duration = DeprecatedTodoListModel.unlockDurationMs(todoList, Date.from(currentInstant))

    assertThat(duration).isEqualTo(1000L)
  }

  @Test
  def unlockDuration_returnsZeroWhenLocked(): Unit = {
    val currentInstant = Instant.now()
    val todoList = DeprecatedTodoListModel.unlock(this.todoListValue, Date.from(currentInstant.minusSeconds(1801)))
      .getOrElse(throw new RuntimeException)

    val duration = DeprecatedTodoListModel.unlockDurationMs(todoList, Date.from(currentInstant))

    assertThat(duration).isEqualTo(0L)
  }
}