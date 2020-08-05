package com.doerapispring.domain

import java.util.{Calendar, Date, TimeZone}

import com.doerapispring.domain.events._

import scala.collection.immutable.LinearSeq
import scala.util.{Success, Try}

// It would seem that this model is an aggregation of a number of different "processes"
// 1) There is the split, ordered list process between todos and deferred todos.
// This includes the move, pull, escalate, and displace features.
// 2) There is the todos process, where they can be updated/deleted/completed
// 3) There is the lock process, where certain attributes can be "hidden"
// 4) There is the timer process, which happens to govern the lock

// Processes 1 & 2 seem appropriately coupled together
// As do processes 3 & 4
// But each of these groupings of processes seem mostly unrelated

// Perhaps I can gain increased clarity on the functional modeling of this domain concept
// by splitting this aggregated process into these 2 groups, minimally

case class SplitTodoListModel(todos: SplitTodoList,
                              deferredTodos: SplitTodoList,
                              lastUnlockedAt: Date = new Date(0L))

// other actions operate across both
// - escalate
// - pull
// - displace

case class SplitTodoList(todos: List[Todo] = List())

case class FixedCapacityTodoList(todoList: SplitTodoList, capacity: Int = 2)

object SplitTodoList {
  def prepend(todoList: SplitTodoList, todoId: TodoId, task: String): SplitTodoList = {
    val todo = new Todo(todoId, task)
    todoList.copy(todos = todo +: todoList.todos)
  }

  def append(todoList: SplitTodoList, todoId: TodoId, task: String): SplitTodoList = {
    val todo = new Todo(todoId, task)
    todoList.copy(todos = todoList.todos :+ todo)
  }

  def delete(todoList: SplitTodoList, todoId: TodoId): SplitTodoList = {
    val todoMatches: Todo => Boolean = todo => todo.getTodoId.equals(todoId)
    todoList.copy(todos = todoList.todos.filterNot(todoMatches))
  }

  def complete(todoList: SplitTodoList, todoId: TodoId): SplitTodoList = {
    delete(todoList, todoId)
  }

  def update(todoList: SplitTodoList, todoId: TodoId, task: String): SplitTodoList = {
    val todoMatches: Todo => Boolean = todo => todo.getTodoId.equals(todoId)
    val indexOfTodo: Int = todoList.todos.indexWhere(todoMatches)
    val before = todoList.todos.slice(0, indexOfTodo)
    val todo :: after = todoList.todos.slice(indexOfTodo, todoList.todos.size)
    val newTodo = new Todo(todo.getTodoId, task)
    todoList.copy(todos = before ::: (newTodo :: after))
  }

  def move(todoList: SplitTodoList, todoId: TodoId, targetTodoId: TodoId): SplitTodoList = {
    val sourceTodoIndex = todoList.todos.indexWhere(todo => todo.getTodoId.equals(todoId))
    val targetTodoIndex = todoList.todos.indexWhere(todo => todo.getTodoId.equals(targetTodoId))
    sourceTodoIndex.compareTo(targetTodoIndex) match {
      case 1 =>
        todoList.copy(todos = todoList.todos.slice(0, targetTodoIndex) ::: todoList.todos(sourceTodoIndex) :: todoList.todos.slice(targetTodoIndex, sourceTodoIndex) ::: todoList.todos.slice(sourceTodoIndex + 1, todoList.todos.size))
      case -1 =>
        todoList.copy(todos = todoList.todos.slice(0, sourceTodoIndex) ::: todoList.todos.slice(sourceTodoIndex + 1, targetTodoIndex + 1) ::: List(todoList.todos(sourceTodoIndex)) ::: todoList.todos.slice(targetTodoIndex + 1, todoList.todos.size))
      case _ => todoList
    }
  }
}

object SplitTodoListModel {
  val MaxSize: Int = 2

  def applyEvent(todoList: SplitTodoListModel, todoListEvent: TodoListEvent): Try[SplitTodoListModel] = {
    val result = todoListEvent match {
      case TodoUpdatedEvent(todoId, task) => update(todoList, new TodoId(todoId), task)
      case TodoCompletedEvent(completedTodoId) => complete(todoList, new TodoId(completedTodoId))
      case TodoDisplacedEvent(todoId, task) => displaceCapability(todoList).flatMap(func => func.apply(new TodoId(todoId), task))
      case TodoDeletedEvent(todoId) => delete(todoList, new TodoId(todoId))
      case TodoAddedEvent(todoId, task) => addCapability(todoList).flatMap(func => func.apply(new TodoId(todoId), task))
      case EscalatedEvent() => escalateCapability(todoList).flatMap(func => func.apply())
      case DeferredTodoAddedEvent(todoId, task) => addDeferred(todoList, new TodoId(todoId), task)
      case TodoMovedEvent(todoId, targetTodoId) => move(todoList, new TodoId(todoId), new TodoId(targetTodoId))
      case PulledEvent() => pullCapability(todoList).flatMap(func => func.apply())
      case UnlockedEvent(unlockedAt) => unlock(todoList, unlockedAt)
    }
    result.recover {
      case e: DomainException =>
        val message = String.format("Error applying event %s to todo list %s", todoListEvent, todoList)
        throw new RuntimeException(message, e)
    }
  }

  def add(todoList: SplitTodoListModel, todoId: TodoId, task: String): Try[SplitTodoListModel] = Try {
    todoList.copy(todos = SplitTodoList.prepend(todoList.todos, todoId, task))
  }

  def addCapability(todoList: SplitTodoListModel): Try[(TodoId, String) => Try[SplitTodoListModel]] = Try {
    if (isFull(todoList)) {
      throw new ListSizeExceededException
    }
    (todoId: TodoId, task: String) => add(todoList, todoId, task)
  }

  def addDeferred(todoList: SplitTodoListModel, todoId: TodoId, task: String): Try[SplitTodoListModel] = Try {
    todoList.copy(deferredTodos = SplitTodoList.append(todoList.deferredTodos, todoId, task))
  }

  def delete(todoList: SplitTodoListModel, todoId: TodoId): Try[SplitTodoListModel] = Try {
    todoList.copy(
      todos = SplitTodoList.delete(todoList.todos, todoId),
      deferredTodos = SplitTodoList.delete(todoList.deferredTodos, todoId)
    )
  }

  def displace(todoList: SplitTodoListModel, todoId: TodoId, task: String): Try[SplitTodoListModel] = Try {
    val todo = new Todo(todoId, task)
    val head :: tail = todoList.todos.todos.reverse
    todoList.copy(todos = SplitTodoList(todo +: tail.reverse), deferredTodos = SplitTodoList(head +: todoList.deferredTodos.todos))
  }

  def displaceCapability(todoList: SplitTodoListModel): Try[(TodoId, String) => Try[SplitTodoListModel]] = Try {
    if (!isFull(todoList)) throw new ListNotFullException
    (todoId: TodoId, task: String) => displace(todoList, todoId, task)
  }

  def update(todoList: SplitTodoListModel, todoId: TodoId, task: String): Try[SplitTodoListModel] = Try {
    todoList.copy(
      todos = SplitTodoList.update(todoList.todos, todoId, task),
      deferredTodos = SplitTodoList.update(todoList.todos, todoId, task)
    )
  }

  def complete(todoList: SplitTodoListModel, todoId: TodoId): Try[SplitTodoListModel] = {
    delete(todoList, todoId)
  }

  def move(todoList: SplitTodoListModel, todoId: TodoId, targetTodoId: TodoId): Try[SplitTodoListModel] = Try {
    todoList.copy(
      todos = SplitTodoList.move(todoList.todos, todoId, targetTodoId),
      deferredTodos = SplitTodoList.move(todoList.deferredTodos, todoId, targetTodoId)
    )
  }

  def getTodos(todoList: SplitTodoListModel): List[Todo] = {
    todoList.todos.todos
  }

  def getDeferredTodos(todoList: SplitTodoListModel, unlockTime: Date): List[Todo] = {
    if (isLocked(todoList, unlockTime)) List.empty else todoList.deferredTodos.todos
  }

  def unlock(todoList: SplitTodoListModel, unlockTime: Date): Try[SplitTodoListModel] = Try {
    todoList.copy(lastUnlockedAt = unlockTime)
  }

  def unlockDurationMs(todoList: SplitTodoListModel, compareTime: Date): Long = {
    val unlockDuration = 1800000L
    val duration = todoList.lastUnlockedAt.toInstant.toEpochMilli + unlockDuration - compareTime.toInstant.toEpochMilli
    if (duration > 0) duration else 0L
  }

  def pull(todoList: SplitTodoListModel): Try[SplitTodoListModel] = Try {
    val numberToPull = MaxSize - todoList.todos.todos.size
    val deferredTodosToPull = todoList.deferredTodos.todos.slice(0, numberToPull)
    val remainingDeferredTodos = todoList.deferredTodos.todos.slice(numberToPull, todoList.deferredTodos.todos.size)
    todoList.copy(todos = SplitTodoList(todoList.todos.todos ++: deferredTodosToPull), deferredTodos = SplitTodoList(remainingDeferredTodos))
  }

  def pullCapability(todoList: SplitTodoListModel): Try[() => Try[SplitTodoListModel]] = Try {
    val isAbleToBeReplenished = !isFull(todoList) && todoList.deferredTodos.todos.nonEmpty
    if (!isAbleToBeReplenished) {
      throw new PullNotAllowedException
    }
    () => pull(todoList)
  }

  def escalate(todoList: SplitTodoListModel): Try[SplitTodoListModel] = {
    val head :: tail = todoList.todos.todos.reverse
    val deferredHead :: deferredTail = todoList.deferredTodos.todos
    Success(todoList.copy(todos = SplitTodoList(tail.reverse :+ deferredHead), deferredTodos = SplitTodoList(head :: deferredTail)))
  }

  def escalateCapability(todoListValue: SplitTodoListModel): Try[() => Try[SplitTodoListModel]] = Try {
    val isAbleToBeEscalated = isFull(todoListValue) && todoListValue.deferredTodos.todos.nonEmpty
    if (!isAbleToBeEscalated) {
      throw new EscalateNotAllowException
    }
    () => escalate(todoListValue)
  }

  def unlockCapability(todoList: SplitTodoListModel, unlockTime: Date): Try[Date => Try[SplitTodoListModel]] = Try {
    val calendar = Calendar.getInstance
    calendar.setTimeZone(TimeZone.getTimeZone("UTC"))
    calendar.setTime(unlockTime)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val isAbleToBeUnlocked = isLocked(todoList, unlockTime) && todoList.lastUnlockedAt.before(calendar.getTime)
    if (!isAbleToBeUnlocked) throw new LockTimerNotExpiredException
    unlockTime => unlock(todoList, unlockTime);
  }

  private def isFull(todoList: SplitTodoListModel): Boolean = {
    todoList.todos.todos.size >= MaxSize
  }

  private def isLocked(todoList: SplitTodoListModel, unlockTime: Date) = {
    todoList.lastUnlockedAt.before(Date.from(unlockTime.toInstant.minusSeconds(1800)))
  }

}

