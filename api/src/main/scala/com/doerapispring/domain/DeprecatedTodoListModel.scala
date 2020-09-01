package com.doerapispring.domain

import java.util.{Calendar, Date, TimeZone}

import com.doerapispring.domain.events._

import scala.util.{Success, Try}

case class DeprecatedTodoListModel(listId: ListId,
                                   profileName: String,
                                   todos: List[DeprecatedTodo] = List(),
                                   lastUnlockedAt: Date = new Date(0L),
                                   demarcationIndex: Integer = 0,
                                   sectionName: String = "now",
                                   deferredSectionName: String = "later")

object DeprecatedTodoListModel {
  val MaxSize: Int = 2

  def applyEvent(todoList: DeprecatedTodoListModel, todoListEvent: TodoListEvent): Try[DeprecatedTodoListModel] = {
    val result = todoListEvent match {
      case DeprecatedTodoUpdatedEvent(todoId, task) => update(todoList, new TodoId(todoId), task)
      case DeprecatedTodoCompletedEvent(completedTodoId) => complete(todoList, new TodoId(completedTodoId))
      case DeprecatedTodoDisplacedEvent(todoId, task) => displaceCapability(todoList).flatMap(func => func.apply(new TodoId(todoId), task))
      case DeprecatedTodoDeletedEvent(todoId) => delete(todoList, new TodoId(todoId))
      case DeprecatedTodoAddedEvent(todoId, task) => addCapability(todoList).flatMap(func => func.apply(new TodoId(todoId), task))
      case EscalatedEvent() => escalateCapability(todoList).flatMap(func => func.apply())
      case DeprecatedDeferredTodoAddedEvent(todoId, task) => addDeferred(todoList, new TodoId(todoId), task)
      case DeprecatedTodoMovedEvent(todoId, targetTodoId) => move(todoList, new TodoId(todoId), new TodoId(targetTodoId))
      case PulledEvent() => pullCapability(todoList).flatMap(func => func.apply())
      case UnlockedEvent(unlockedAt) => unlock(todoList, unlockedAt)
    }
    result.recover {
      case e: DomainException =>
        val message = String.format("Error applying event %s to todo list %s", todoListEvent, todoList)
        throw new RuntimeException(message, e)
    }
  }

  def add(todoList: DeprecatedTodoListModel, todoId: TodoId, task: String): Try[DeprecatedTodoListModel] = Try {
    if (alreadyExists(todoList, task)) {
      throw new DuplicateTodoException
    }
    val todo = new DeprecatedTodo(todoId, task)
    todoList.copy(todos = todo +: todoList.todos, demarcationIndex = todoList.demarcationIndex + 1)
  }

  def addCapability(todoList: DeprecatedTodoListModel): Try[(TodoId, String) => Try[DeprecatedTodoListModel]] = Try {
    if (isFull(todoList)) {
      throw new ListSizeExceededException
    }
    (todoId: TodoId, task: String) => add(todoList, todoId, task)
  }

  def addDeferred(todoList: DeprecatedTodoListModel, todoId: TodoId, task: String): Try[DeprecatedTodoListModel] = Try {
    if (alreadyExists(todoList, task)) {
      throw new DuplicateTodoException
    }
    val todo = new DeprecatedTodo(todoId, task)
    todoList.copy(todos = todoList.todos :+ todo)
  }

  def delete(todoList: DeprecatedTodoListModel, todoId: TodoId): Try[DeprecatedTodoListModel] = Try {
    val todoMatches: DeprecatedTodo => Boolean = todo => todo.getTodoId.equals(todoId)
    if (!todoList.todos.exists(todoMatches)) throw new TodoNotFoundException
    val indexOfTodo = todoList.todos.indexWhere(todoMatches)
    val newDemarcationIndex: Int = if (indexOfTodo < todoList.demarcationIndex) todoList.demarcationIndex - 1 else todoList.demarcationIndex
    todoList.copy(todos = todoList.todos.filterNot(todoMatches), demarcationIndex = newDemarcationIndex)
  }

  def displace(todoList: DeprecatedTodoListModel, todoId: TodoId, task: String): Try[DeprecatedTodoListModel] = Try {
    if (alreadyExists(todoList, task)) throw new DuplicateTodoException
    val todo = new DeprecatedTodo(todoId, task)
    todoList.copy(todos = todo +: todoList.todos)
  }

  def displaceCapability(todoList: DeprecatedTodoListModel): Try[(TodoId, String) => Try[DeprecatedTodoListModel]] = Try {
    if (!isFull(todoList)) throw new ListNotFullException
    (todoId: TodoId, task: String) => displace(todoList, todoId, task)
  }

  def update(todoList: DeprecatedTodoListModel, todoId: TodoId, task: String): Try[DeprecatedTodoListModel] = Try {
    if (alreadyExists(todoList, task)) throw new DuplicateTodoException
    val todoMatches: DeprecatedTodo => Boolean = todo => todo.getTodoId.equals(todoId)
    val todo: DeprecatedTodo = todoList.todos.find(todoMatches).getOrElse(throw new TodoNotFoundException)
    todo.setTask(task)
    todoList.copy(todos = todoList.todos)
  }

  def complete(todoList: DeprecatedTodoListModel, todoId: TodoId): Try[DeprecatedTodoListModel] = {
    delete(todoList, todoId)
  }

  def move(todoList: DeprecatedTodoListModel, todoId: TodoId, targetTodoId: TodoId): Try[DeprecatedTodoListModel] = Try {
    val sourceTodoMatches: DeprecatedTodo => Boolean = todo => todo.getTodoId.equals(todoId)
    val targetTodoMatches: DeprecatedTodo => Boolean = todo => todo.getTodoId.equals(targetTodoId)
    val sourceTodo: DeprecatedTodo = todoList.todos.find(sourceTodoMatches).getOrElse(throw new TodoNotFoundException)
    val targetTodo: DeprecatedTodo = todoList.todos.find(targetTodoMatches).getOrElse(throw new TodoNotFoundException)
    val sourceTodoIndex = todoList.todos.indexOf(sourceTodo)
    val targetTodoIndex = todoList.todos.indexOf(targetTodo)
    sourceTodoIndex.compareTo(targetTodoIndex) match {
      case 1 =>
        todoList.copy(todos = todoList.todos.slice(0, targetTodoIndex) ::: sourceTodo :: todoList.todos.slice(targetTodoIndex, sourceTodoIndex) ::: todoList.todos.slice(sourceTodoIndex + 1, todoList.todos.size))
      case -1 =>
        todoList.copy(todos = todoList.todos.slice(0, sourceTodoIndex) ::: todoList.todos.slice(sourceTodoIndex + 1, targetTodoIndex + 1) ::: List(sourceTodo) ::: todoList.todos.slice(targetTodoIndex + 1, todoList.todos.size))
      case _ => todoList
    }
  }

  def getTodos(todoList: DeprecatedTodoListModel): List[DeprecatedTodo] = {
    todoList.todos.slice(0, todoList.demarcationIndex)
  }

  def getDeferredTodos(todoList: DeprecatedTodoListModel, unlockTime: Date): List[DeprecatedTodo] = {
    if (isLocked(todoList, unlockTime)) List.empty else todoList.todos.slice(todoList.demarcationIndex, todoList.todos.size)
  }

  def unlock(todoList: DeprecatedTodoListModel, unlockTime: Date): Try[DeprecatedTodoListModel] = Try {
    todoList.copy(lastUnlockedAt = unlockTime)
  }

  def unlockDurationMs(todoList: DeprecatedTodoListModel, compareTime: Date): Long = {
    val unlockDuration = 1800000L
    val duration = todoList.lastUnlockedAt.toInstant.toEpochMilli + unlockDuration - compareTime.toInstant.toEpochMilli
    if (duration > 0) duration else 0L
  }

  def pull(todoList: DeprecatedTodoListModel): Try[DeprecatedTodoListModel] = Try {
    todoList.copy(demarcationIndex = Math.min(todoList.todos.size, MaxSize))
  }

  def pullCapability(todoList: DeprecatedTodoListModel): Try[() => Try[DeprecatedTodoListModel]] = Try {
    val isAbleToBeReplenished = !isFull(todoList) && todoList.todos.slice(todoList.demarcationIndex, todoList.todos.size).nonEmpty
    if (!isAbleToBeReplenished) {
      throw new PullNotAllowedException
    }
    () => pull(todoList)
  }

  def escalate(todoList: DeprecatedTodoListModel): Try[DeprecatedTodoListModel] = {
    val first :: second :: third :: rest = todoList.todos
    Success(todoList.copy(todos = first :: third :: second :: rest))
  }

  def escalateCapability(todoListValue: DeprecatedTodoListModel): Try[() => Try[DeprecatedTodoListModel]] = Try {
    val isAbleToBeEscalated = isFull(todoListValue) && todoListValue.todos.slice(todoListValue.demarcationIndex, todoListValue.todos.size).nonEmpty
    if (!isAbleToBeEscalated) {
      throw new EscalateNotAllowException
    }
    () => escalate(todoListValue)
  }

  def unlockCapability(todoList: DeprecatedTodoListModel, unlockTime: Date): Try[Date => Try[DeprecatedTodoListModel]] = Try {
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

  private def isFull(todoList: DeprecatedTodoListModel): Boolean = {
    todoList.todos.slice(0, todoList.demarcationIndex).size >= MaxSize
  }

  private def isLocked(todoList: DeprecatedTodoListModel, unlockTime: Date) = {
    todoList.lastUnlockedAt.before(Date.from(unlockTime.toInstant.minusSeconds(1800)))
  }

  private def alreadyExists(todoList: DeprecatedTodoListModel, task: String): Boolean = {
    todoList.todos
      .map(todo => todo.getTask)
      .contains(task)
  }
}

