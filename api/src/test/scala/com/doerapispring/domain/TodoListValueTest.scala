package com.doerapispring.domain

import java.time.Instant
import java.util.Date

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.{assertThat, assertThatThrownBy}
import org.assertj.core.api.ThrowableAssert.ThrowingCallable
import org.junit.Test

import scala.jdk.CollectionConverters._

//noinspection AccessorLikeMethodIsUnit
class  TodoListValueTest {
  private val todoListValue: TodoListValue = new TodoListValue(scala.collection.immutable.List.empty, "someName", Date.from(Instant.EPOCH), 0)

  @Test
  def add_addsToNowList(): Unit = {
    val todoListValue = TodoListValue.add(this.todoListValue, new TodoId("someId"), "someTask")._1
    assertThat(TodoListValue.getTodos(todoListValue).asJavaCollection).containsExactly(new Todo(new TodoId("someId"), "someTask"))
  }

  @Test
  def add_addsToNowList_beforeFirstTodo(): Unit = {
    val todoListValue = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "someTask")._1)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "someOtherTask")._1)
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListValue.getTodos(todoListValue).asJavaCollection).extracting("task").containsExactly("someOtherTask", "someTask")
  }

  @Test
  def add_whenListAlreadyContainsTask_doesNotAdd_throwsDuplicateTodoException(): Unit = {
    val todoList = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "sameTask")._1)
      .getOrElse(throw new RuntimeException)
    val callable = new ThrowingCallable {
      override def call(): Unit = {
        TodoListValue.add(todoList, new TodoId("someId"), "sameTask")
      }
    }
    assertThatThrownBy(callable).isInstanceOf(classOf[DuplicateTodoException])
  }

  @Test
  def add_whenListIsFull_doesNotAdd_throwsListSizeExceededException(): Unit = {
    val todoList = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "someTask")._1)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "someOtherTask")._1)
      .getOrElse(throw new RuntimeException)
    val callable = new ThrowingCallable {
      override def call(): Unit = {
        TodoListValue.add(todoList, new TodoId("someId"), "stillAnotherTask")
      }
    }
    assertThatThrownBy(callable).isInstanceOf(classOf[ListSizeExceededException])
  }

  @Test
  def addDeferred_addsToLaterList(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.addDeferred(todoList, new TodoId("someId"), "someTask")._1)
      .map(todoList => TodoListValue.unlock(todoList, now)._1)
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListValue.getDeferredTodos(todoListValue, now).asJavaCollection).containsExactly(new Todo(new TodoId("someId"), "someTask"))
  }

  @Test
  def addDeferred_addsToLaterList_afterLastTodo(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.addDeferred(todoList, new TodoId("someId"), "someTask")._1)
      .map(todoList => TodoListValue.addDeferred(todoList, new TodoId("someId"), "someOtherTask")._1)
      .map(todoList => TodoListValue.unlock(todoList, now)._1)
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListValue.getDeferredTodos(todoListValue, now).asJavaCollection).extracting("task").containsExactly("someTask", "someOtherTask")
  }

  @Test
  def addDeferred_whenListAlreadyContainsTask_doesNotAdd_throwsDuplicateTodoException(): Unit = {
    val todoList = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.addDeferred(todoList, new TodoId("someId"), "sameTask")._1)
      .getOrElse(throw new RuntimeException)
    val callable = new ThrowingCallable {
      override def call(): Unit = {
        TodoListValue.addDeferred(todoList, new TodoId("someId"), "sameTask")
      }
    }
    assertThatThrownBy(callable).isInstanceOf(classOf[DuplicateTodoException])
  }

  @Test
  def delete_whenTodoWithIdentifierExists_removesTodo(): Unit = {
    val todoId = new TodoId("someId")
    val todoListValue = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.add(todoList, todoId, "someTask")._1)
      .map(todoList => TodoListValue.delete(todoList, todoId)._1)
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListValue.getTodos(todoListValue).asJavaCollection).isEmpty()
  }

  @Test
  def delete_whenTodoWithIdentifierExists_removesDeferredTodo(): Unit = {
    val now = Date.from(Instant.now())
    val todoId = new TodoId("someId")
    val todoListValue = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.add(todoList, todoId, "someTask")._1)
      .map(todoList => TodoListValue.delete(todoList, todoId)._1)
      .map(todoList => TodoListValue.unlock(todoList, now)._1)
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListValue.getDeferredTodos(todoListValue, now).asJavaCollection).isEmpty()
  }

  @Test
  def delete_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException(): Unit = {
    val callable = new ThrowingCallable {
      override def call(): Unit = {
        TodoListValue.delete(todoListValue, new TodoId("someBogusIdentifier"))
      }
    }
    assertThatThrownBy(callable).isInstanceOf(classOf[TodoNotFoundException])
  }

  @Test
  def displace_whenListIsNotFull_throwsListNotFullException(): Unit = {
    val callable = new ThrowingCallable {
      override def call(): Unit = {
        TodoListValue.displace(todoListValue, new TodoId("whateverTodoId"), "someTask")
      }
    }
    assertThatThrownBy(callable).isInstanceOf(classOf[ListNotFullException])
  }

  @Test
  def displace_whenListIsFull_whenTaskAlreadyExists_throwsDuplicateTodoException(): Unit = {
    val todoList = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "task")._1)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "sameTask")._1)
      .getOrElse(throw new RuntimeException)
    val callable = new ThrowingCallable {
      override def call(): Unit = {
        TodoListValue.displace(todoList, new TodoId("someId"), "sameTask")
      }
    }
    assertThatThrownBy(callable).isInstanceOf(classOf[DuplicateTodoException])
  }

  @Test
  def displace_whenPostponedListIsEmpty_replacesTodo_andPushesItIntoPostponedListWithCorrectPositioning(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.add(todoList, new TodoId("1"), "someNowTask")._1)
      .map(todoList => TodoListValue.add(todoList, new TodoId("2"), "someOtherNowTask")._1)
      .map(todoList => TodoListValue.displace(todoList, new TodoId("3"), "displace it")._1)
      .map(todoList => TodoListValue.unlock(todoList, now)._1)
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListValue.getTodos(todoListValue).asJavaCollection).containsExactly(
      new Todo(new TodoId("3"), "displace it"),
      new Todo(new TodoId("2"), "someOtherNowTask"))
    assertThat(TodoListValue.getDeferredTodos(todoListValue, now).asJavaCollection).containsExactly(new Todo(new TodoId("1"), "someNowTask"))
  }

  @Test
  def displace_whenPostponedIsNotEmpty_replacesTodo_andPushesItIntoPostponedListWithCorrectPositioning(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.add(todoList, new TodoId("1"), "someNowTask")._1)
      .map(todoList => TodoListValue.add(todoList, new TodoId("2"), "someOtherNowTask")._1)
      .map(todoList => TodoListValue.addDeferred(todoList, new TodoId("3"), "someLaterTask")._1)
      .map(todoList => TodoListValue.displace(todoList, new TodoId("4"), "displace it")._1)
      .map(todoList => TodoListValue.unlock(todoList, now)._1)
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListValue.getTodos(todoListValue).asJavaCollection).containsExactly(
      new Todo(new TodoId("4"), "displace it"),
      new Todo(new TodoId("2"), "someOtherNowTask"))
    assertThat(TodoListValue.getDeferredTodos(todoListValue, now).asJavaCollection).containsExactly(
      new Todo(new TodoId("1"), "someNowTask"),
      new Todo(new TodoId("3"), "someLaterTask"))
  }

  @Test
  def update_whenTodoWithIdentifierExists_updatesTodo(): Unit = {
    val todoId = new TodoId("someId")
    val todoListValue = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.add(todoList, todoId, "someTask")._1)
      .map(todoList => TodoListValue.update(todoList, todoId, "someOtherTask")._1)
      .getOrElse(throw new RuntimeException)
    val todo :: _ = TodoListValue.getTodos(todoListValue)
    Assertions.assertThat(todo.getTask).isEqualTo("someOtherTask")
  }

  @Test
  def update_whenTaskAlreadyExists_throwsDuplicateTodoException(): Unit = {
    val todoId = new TodoId("someId")
    val todoList = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.add(todoList, todoId, "sameTask")._1)
      .getOrElse(throw new RuntimeException)

    val callable = new ThrowingCallable {
      override def call(): Unit = {
        TodoListValue.update(todoList, todoId, "sameTask")
      }
    }
    assertThatThrownBy(callable).isInstanceOf(classOf[DuplicateTodoException])
  }

  @Test
  def update_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException(): Unit = {
    val callable = new ThrowingCallable {
      override def call(): Unit = {
        TodoListValue.update(todoListValue, new TodoId("bananaPudding"), "sameTask")
      }
    }
    assertThatThrownBy(callable).isInstanceOf(classOf[TodoNotFoundException])
  }

  @Test
  def complete_whenTodoWithIdentifierExists_removesTodoFromMatchingList_returnsCompletedTodo(): Unit = {
    val todoId = new TodoId("someId")
    val todoListValue = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.add(todoList, todoId, "someTask")._1)
      .map(todoList => TodoListValue.complete(todoList, todoId)._1)
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListValue.getTodos(todoListValue).asJavaCollection).isEmpty()
  }

  @Test
  def complete_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException(): Unit = {
    val callable = new ThrowingCallable {
      override def call(): Unit = TodoListValue.complete(todoListValue, new TodoId("someBogusIdentifier"))
    }
    assertThatThrownBy(callable).isInstanceOf(classOf[TodoNotFoundException])
  }

  @Test
  def move_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException(): Unit = {
    val callable = new ThrowingCallable {
      override def call(): Unit = TodoListValue.move(todoListValue, new TodoId("junk"), new TodoId("bogus"))
    }
    assertThatThrownBy(callable).isInstanceOf(classOf[TodoNotFoundException])
  }

  @Test
  def move_whenTodoWithIdentifierExists_whenTargetExists_movesTodoDown(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue1 = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "now1")._1)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "now2")._1)
      .map(todoList => TodoListValue.unlock(todoList, now)._1)
      .getOrElse(throw new RuntimeException)
    val tasks = List("someTask", "anotherTask", "yetAnotherTask", "evenYetAnotherTask")
    val todoListValue2 = tasks.zipWithIndex.foldLeft(todoListValue1) { (todoList: TodoListValue, next) =>
      val (task, i) = next
      TodoListValue.addDeferred(todoList, new TodoId(String.valueOf(i)), task)._1
    }
    val todoListValue3 = Option.apply(todoListValue2)
      .map(todoList => TodoListValue.move(todoList, new TodoId("0"), new TodoId("2"))._1)
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListValue.getDeferredTodos(todoListValue3, now).asJavaCollection).extracting("task")
      .containsExactly("anotherTask", "yetAnotherTask", "someTask", "evenYetAnotherTask")
  }

  @Test
  def move_whenTodoWithIdentifierExists_whenTargetExists_movesTodoUp(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue1 = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "now1")._1)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "now2")._1)
      .map(todoList => TodoListValue.unlock(todoList, now)._1)
      .getOrElse(throw new RuntimeException)
    val tasks = List("someTask", "anotherTask", "yetAnotherTask", "evenYetAnotherTask")
    val todoListValue2 = tasks.zipWithIndex.foldLeft(todoListValue1) { (todoList: TodoListValue, next) =>
      val (task, i) = next
      TodoListValue.addDeferred(todoList, new TodoId(String.valueOf(i)), task)._1
    }
    val todoListValue3 = Option.apply(todoListValue2)
      .map(todoList => TodoListValue.move(todoList, new TodoId("3"), new TodoId("1"))._1)
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListValue.getDeferredTodos(todoListValue3, now).asJavaCollection).extracting("task")
      .containsExactly("someTask", "evenYetAnotherTask", "anotherTask", "yetAnotherTask")
  }

  @Test
  def move_beforeOrAfter_whenTodoWithIdentifierExists_whenTargetExists_whenOriginalAndTargetPositionsAreSame_doesNothing(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue1 = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "now1")._1)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "now2")._1)
      .map(todoList => TodoListValue.unlock(todoList, now)._1)
      .getOrElse(throw new RuntimeException)
    val tasks = List("someTask", "anotherTask", "yetAnotherTask", "evenYetAnotherTask")
    val todoListValue2 = tasks.zipWithIndex.foldLeft(todoListValue1) { (todoList: TodoListValue, next) =>
      val (task, i) = next
      TodoListValue.addDeferred(todoList, new TodoId(String.valueOf(i)), task)._1
    }
    val todoListValue3 = Option.apply(todoListValue2)
      .map(todoList => TodoListValue.move(todoList, new TodoId("0"), new TodoId("0"))._1)
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListValue.getDeferredTodos(todoListValue3, now).asJavaCollection).extracting("task").containsExactlyElementsOf(tasks.asJavaCollection)
  }

  @Test
  @throws[Exception]
  def pull_whenThereAreNoImmediateTodos_fillsFromPostponedList(): Unit = {
    val todoListValue = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.addDeferred(todoList, new TodoId("someId"), "firstLater")._1)
      .map(todoList => TodoListValue.addDeferred(todoList, new TodoId("someId"), "secondLater")._1)
      .map(todoList => TodoListValue.pull(todoList)._1)
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListValue.getTodos(todoListValue).asJavaCollection).extracting("task").containsExactly("firstLater", "secondLater")
  }

  @Test
  @throws[Exception]
  def pull_whenThereAreNoImmediateTodos_andOneDeferredTodo_fillsFromPostponedList(): Unit = {
    val todoListValue = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.addDeferred(todoList, new TodoId("someId"), "firstLater")._1)
      .map(todoList => TodoListValue.pull(todoList)._1)
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListValue.getTodos(todoListValue).asJavaCollection).extracting("task").containsExactly("firstLater")
  }

  @Test
  @throws[Exception]
  def pull_whenThereAreLessImmediateTodosThanMaxSize_fillsFromPostponedList_withAsManyTodosAsTheDifference(): Unit = {
    val todoListValue = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "firstNow")._1)
      .map(todoList => TodoListValue.addDeferred(todoList, new TodoId("someId"), "firstLater")._1)
      .map(todoList => TodoListValue.addDeferred(todoList, new TodoId("someId"), "secondLater")._1)
      .map(todoList => TodoListValue.addDeferred(todoList, new TodoId("someId"), "thirdLater")._1)
      .map(todoList => TodoListValue.pull(todoList)._1)
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListValue.getTodos(todoListValue).asJavaCollection).extracting("task").containsExactly("firstNow", "firstLater")
  }

  @Test
  @throws[Exception]
  def pull_whenThereAreAsManyImmediateTodosAsMaxSize_doesNotFillFromPostponedList(): Unit = {
    val todoListValue = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "someTask")._1)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "someOtherTask")._1)
      .map(todoList => TodoListValue.addDeferred(todoList, new TodoId("someId"), "firstLater")._1)
      .map(todoList => TodoListValue.pull(todoList)._1)
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListValue.getTodos(todoListValue).asJavaCollection).extracting("task").containsExactly("someOtherTask", "someTask")
  }

  @Test
  def pull_whenThereIsASourceList_whenThereAreLessTodosThanMaxSize_whenSourceListIsEmpty_doesNotFillListFromSource(): Unit = {
    val todoListValue =TodoListValue.pull(this.todoListValue)._1
    assertThat(TodoListValue.getTodos(todoListValue).asJavaCollection).isEmpty()
  }

  @Test
  @throws[Exception]
  def escalate_swapsPositionsOfLastTodoAndFirstDeferredTodo(): Unit = {
    val now = Date.from(Instant.now())
    val todoListValue = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "will be deferred after escalate")._1)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "some task")._1)
      .map(todoList => TodoListValue.addDeferred(todoList, new TodoId("someId"), "will no longer be deferred after escalate")._1)
      .map(todoList => TodoListValue.escalate(todoList)._1)
      .map(todoList => TodoListValue.unlock(todoList, now)._1)
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListValue.getTodos(todoListValue).asJavaCollection).containsExactly(
      new Todo(new TodoId("someId"), "some task"),
      new Todo(new TodoId("someId"), "will no longer be deferred after escalate"))
    assertThat(TodoListValue.getDeferredTodos(todoListValue, now).asJavaCollection).containsExactly(
      new Todo(new TodoId("someId"), "will be deferred after escalate"))
  }

  @Test
  @throws[Exception]
  def escalate_whenListIsNotAbleToBeEscalated_throwsEscalateNotAllowedException_becauseItIsNotFull(): Unit = {
    val todoListValue = TodoListValue.add(this.todoListValue, new TodoId("someId"), "someTask")._1
    val callable = new ThrowingCallable {
      override def call(): Unit = {
        TodoListValue.escalate(todoListValue)
      }
    }
    assertThatThrownBy(callable).isInstanceOf(classOf[EscalateNotAllowException])
  }

  @Test
  @throws[Exception]
  def escalate_whenListIsNotAbleToBeEscalated_throwsEscalateNotAllowedException_becauseItIsFullButHasNoDeferredTodos(): Unit = {
    val todoListValue = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "task1")._1)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "task2")._1)
      .getOrElse(throw new RuntimeException)
    val callable = new ThrowingCallable {
      override def call(): Unit = {
        TodoListValue.escalate(todoListValue)
      }
    }
    assertThatThrownBy(callable).isInstanceOf(classOf[EscalateNotAllowException])
  }

  @Test
  @throws[Exception]
  def isFull_whenCountOfTodos_isGreaterThanOrEqualToMaxSize_returnsTrue(): Unit = {
    val todoListValue = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "task1")._1)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "task2")._1)
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListValue.isFull(todoListValue)).isTrue
  }

  @Test
  def isFull_whenCountOfTodos_isLessThanMaxSize_returnsFalse(): Unit = {
    assertThat(TodoListValue.isFull(this.todoListValue)).isFalse
  }

  @Test def isAbleToBeUnlocked_whenThereAreNoListUnlocks_returnsTrue(): Unit = {
    assertThat(TodoListValue.isAbleToBeUnlocked(this.todoListValue, Date.from(Instant.now()))).isTrue
  }

  @Test
  @throws[Exception]
  def isAbleToBeUnlocked_whenThereAreListUnlocks_whenFirstListUnlockWasCreatedToday_returnsFalse(): Unit = {
    val todoListValue = this.todoListValue.copy(lastUnlockedAt = Date.from(Instant.ofEpochMilli(618537600000L))) // Tuesday, August 8, 1989 12:00:00 AM

    val instant = Instant.ofEpochMilli(618623999999L) // Tuesday, August 8, 1989 11:59:59 PM
    assertThat(TodoListValue.isAbleToBeUnlocked(todoListValue, Date.from(instant))).isFalse
  }

  @Test
  @throws[Exception]
  def isAbleToBeUnlocked_whenThereAreListUnlocks_whenFirstListUnlockWasCreatedBeforeToday_returnsTrue(): Unit = {
    val todoListValue = this.todoListValue.copy(lastUnlockedAt = Date.from(Instant.ofEpochMilli(618429599999L))) // Monday, August 7, 1989 11:29:59 PM

    val instant = Instant.ofEpochMilli(618537600000L) // Tuesday, August 8, 1989 12:00:00 AM
    assertThat(TodoListValue.isAbleToBeUnlocked(todoListValue, Date.from(instant))).isTrue
  }

  @Test
  @throws[Exception]
  def isAbleToBeUnlocked_whenThereAreListUnlocks_whenFirstListUnlockWasCreatedBeforeToday_butListIsStillUnlocked_returnsFalse(): Unit = {
    val todoListValue = this.todoListValue.copy(lastUnlockedAt = Date.from(Instant.ofEpochMilli(618536700000L))) // Monday, August 7, 1989 11:45:00 PM

    val instant = Instant.ofEpochMilli(618537900000L) // Tuesday, August 8, 1989 12:05:00 AM
    assertThat(TodoListValue.isAbleToBeUnlocked(todoListValue, Date.from(instant))).isFalse
  }

  @Test
  @throws[Exception]
  def isAbleToBeReplenished_whenThereAreDeferredTodos_andTheListIsNotFull_returnsTrue(): Unit = {
    val todoListValue = TodoListValue.addDeferred(this.todoListValue, new TodoId("someId"), "someTask")._1
    assertThat(TodoListValue.isAbleToBeReplenished(todoListValue)).isTrue
  }

  @Test
  @throws[Exception]
  def isAbleToBeReplenished_whenThereAreDeferredTodos_andTheListIsFull_returnsFalse(): Unit = {
    val todoListValue = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "todo1")._1)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "todo2")._1)
      .map(todoList => TodoListValue.addDeferred(todoList, new TodoId("someId"), "someTask")._1)
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListValue.isAbleToBeReplenished(todoListValue)).isFalse
  }

  @Test
  @throws[Exception]
  def isAbleToBeReplenished_whenThereAreNoDeferredTodos_andTheListIsNotFull_returnsFalse(): Unit = {
    val todoListValue = TodoListValue.add(this.todoListValue, new TodoId("someId"), "todo1")._1
    assertThat(TodoListValue.isAbleToBeReplenished(todoListValue)).isFalse
  }

  @Test
  @throws[Exception]
  def isAbleToBeEscalated_whenTheListIsFull_andThereAreDeferredTodos_returnsTrue(): Unit = {
    val todoListValue = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "task 1")._1)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "task 2")._1)
      .map(todoList => TodoListValue.addDeferred(todoList, new TodoId("someId"), "task 3")._1)
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListValue.isAbleToBeEscalated(todoListValue)).isTrue
  }

  @Test
  @throws[Exception]
  def isAbleToBeEscalated_whenTheListIsNotFull_andThereAreDeferredTodos_returnsFalse(): Unit = {
    val todoListValue = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "task 1")._1)
      .map(todoList => TodoListValue.addDeferred(todoList, new TodoId("someId"), "task 2")._1)
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListValue.isAbleToBeEscalated(todoListValue)).isFalse
  }

  @Test
  @throws[Exception]
  def isAbleToBeEscalated_whenTheListIsFull_andThereAreNoDeferredTodos_returnsFalse(): Unit = {
    val todoListValue = Option.apply(this.todoListValue)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "task 1")._1)
      .map(todoList => TodoListValue.add(todoList, new TodoId("someId"), "task 2")._1)
      .getOrElse(throw new RuntimeException)
    assertThat(TodoListValue.isAbleToBeEscalated(todoListValue)).isFalse
  }
}