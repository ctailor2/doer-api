package com.doerapispring.domain

import java.util.{Calendar, Date, TimeZone}

import com.doerapispring.domain.events._

import scala.util.{Success, Try}

case class TodoListModel(listId: ListId,
                         profileName: String,
                         todos: List[Todo] = List(),
                         lastUnlockedAt: Date = new Date(0L),
                         demarcationIndex: Integer = 0,
                         sectionName: String = "now",
                         deferredSectionName: String = "later")

object TodoListModel {
  val MaxSize: Int = 2

  def applyEvent(todoList: TodoListModel, todoListEvent: TodoListEvent): Try[TodoListModel] = {
    val result: Try[(TodoListModel, TodoListEvent)] = todoListEvent match {
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
    }.map {
      case (todoList, _) => todoList
    }
  }

  def add(todoList: TodoListModel, todoId: TodoId, task: String): Try[(TodoListModel, TodoListEvent)] = Try {
    if (alreadyExists(todoList, task)) {
      throw new DuplicateTodoException
    }
    val todo = new Todo(todoId, task)
    val result = todoList.copy(todos = todo +: todoList.todos, demarcationIndex = todoList.demarcationIndex + 1)
    val event = TodoAddedEvent(todoId.getIdentifier, task)
    (result, event)
  }

  def addCapability(todoList: TodoListModel): Try[(TodoId, String) => Try[(TodoListModel, TodoListEvent)]] = Try {
    if (isFull(todoList)) {
      throw new ListSizeExceededException
    }
    (todoId: TodoId, task: String) => add(todoList, todoId, task)
  }

  def addCapabilityDeconstructed(todoList: TodoListModel): Try[TodoId => String => Try[(TodoListModel, TodoListEvent)]] = Try {
    if (isFull(todoList)) {
      throw new ListSizeExceededException
    }
    todoId => task => add(todoList, todoId, task);
  }

  def addDeferred(todoList: TodoListModel, todoId: TodoId, task: String): Try[(TodoListModel, TodoListEvent)] = Try {
    if (alreadyExists(todoList, task)) {
      throw new DuplicateTodoException
    }
    val todo = new Todo(todoId, task)
    val result = todoList.copy(todos = todoList.todos :+ todo)
    val event = DeferredTodoAddedEvent(todoId.getIdentifier, task)
    (result, event)
  }

  def delete(todoList: TodoListModel, todoId: TodoId): Try[(TodoListModel, TodoListEvent)] = Try {
    val todoMatches: Todo => Boolean = todo => todo.getTodoId.equals(todoId)
    if (!todoList.todos.exists(todoMatches)) throw new TodoNotFoundException
    val indexOfTodo = todoList.todos.indexWhere(todoMatches)
    val newDemarcationIndex: Int = if (indexOfTodo < todoList.demarcationIndex) todoList.demarcationIndex - 1 else todoList.demarcationIndex
    val result = todoList.copy(todos = todoList.todos.filterNot(todoMatches), demarcationIndex = newDemarcationIndex)
    val event = TodoDeletedEvent(todoId.getIdentifier)
    (result, event)
  }

  def displace(todoList: TodoListModel, todoId: TodoId, task: String): Try[(TodoListModel, TodoListEvent)] = Try {
    if (alreadyExists(todoList, task)) throw new DuplicateTodoException
    val todo = new Todo(todoId, task)
    val result = todoList.copy(todos = todo +: todoList.todos)
    val event = TodoDisplacedEvent(todoId.getIdentifier, task)
    (result, event)
  }

  def displaceCapability(todoList: TodoListModel): Try[(TodoId, String) => Try[(TodoListModel, TodoListEvent)]] = Try {
    if (!isFull(todoList)) throw new ListNotFullException
    (todoId: TodoId, task: String) => displace(todoList, todoId, task)
  }

  def update(todoList: TodoListModel, todoId: TodoId, task: String): Try[(TodoListModel, TodoListEvent)] = Try {
    if (alreadyExists(todoList, task)) throw new DuplicateTodoException
    val todoMatches: Todo => Boolean = todo => todo.getTodoId.equals(todoId)
    val todo: Todo = todoList.todos.find(todoMatches).getOrElse(throw new TodoNotFoundException)
    todo.setTask(task)
    val result = todoList.copy(todos = todoList.todos)
    val event = TodoUpdatedEvent(todoId.getIdentifier, task)
    (result, event)
  }

  def complete(todoList: TodoListModel, todoId: TodoId): Try[(TodoListModel, TodoListEvent)] = {
    val result = delete(todoList, todoId)
    val event = TodoCompletedEvent(todoId.getIdentifier)
    result.map(tuple => tuple.copy(_2 = event))
  }

  def move(todoList: TodoListModel, todoId: TodoId, targetTodoId: TodoId): Try[(TodoListModel, TodoListEvent)] = Try {
    val sourceTodoMatches: Todo => Boolean = todo => todo.getTodoId.equals(todoId)
    val targetTodoMatches: Todo => Boolean = todo => todo.getTodoId.equals(targetTodoId)
    val sourceTodo: Todo = todoList.todos.find(sourceTodoMatches).getOrElse(throw new TodoNotFoundException)
    val targetTodo: Todo = todoList.todos.find(targetTodoMatches).getOrElse(throw new TodoNotFoundException)
    val sourceTodoIndex = todoList.todos.indexOf(sourceTodo)
    val targetTodoIndex = todoList.todos.indexOf(targetTodo)
    val result = sourceTodoIndex.compareTo(targetTodoIndex) match {
      case 1 =>
        todoList.copy(todos = todoList.todos.slice(0, targetTodoIndex) ::: sourceTodo :: todoList.todos.slice(targetTodoIndex, sourceTodoIndex) ::: todoList.todos.slice(sourceTodoIndex + 1, todoList.todos.size))
      case -1 =>
        todoList.copy(todos = todoList.todos.slice(0, sourceTodoIndex) ::: todoList.todos.slice(sourceTodoIndex + 1, targetTodoIndex + 1) ::: List(sourceTodo) ::: todoList.todos.slice(targetTodoIndex + 1, todoList.todos.size))
      case _ => todoList
    }
    val event = TodoMovedEvent(todoId.getIdentifier, targetTodoId.getIdentifier)
    (result, event)
  }

  def getTodos(todoList: TodoListModel): List[Todo] = {
    todoList.todos.slice(0, todoList.demarcationIndex)
  }

  def getDeferredTodos(todoList: TodoListModel, unlockTime: Date): List[Todo] = {
    if (isLocked(todoList, unlockTime)) List.empty else todoList.todos.slice(todoList.demarcationIndex, todoList.todos.size)
  }

  def unlock(todoList: TodoListModel, unlockTime: Date): Try[(TodoListModel, TodoListEvent)] = Try {
    val result = todoList.copy(lastUnlockedAt = unlockTime)
    val event = UnlockedEvent(unlockTime)
    (result, event)
  }

  def unlockDurationMs(todoList: TodoListModel, compareTime: Date): Long = {
    val unlockDuration = 1800000L
    val duration = todoList.lastUnlockedAt.toInstant.toEpochMilli + unlockDuration - compareTime.toInstant.toEpochMilli
    if (duration > 0) duration else 0L
  }

  def pull(todoList: TodoListModel): Try[(TodoListModel, TodoListEvent)] = Try {
    val result = todoList.copy(demarcationIndex = Math.min(todoList.todos.size, MaxSize))
    val event = PulledEvent()
    (result, event)
  }

  def pullCapability(todoList: TodoListModel): Try[() => Try[(TodoListModel, TodoListEvent)]] = Try {
    val isAbleToBeReplenished = !isFull(todoList) && todoList.todos.slice(todoList.demarcationIndex, todoList.todos.size).nonEmpty
    if (!isAbleToBeReplenished) {
      throw new PullNotAllowedException
    }
    () => pull(todoList)
  }

  def escalate(todoList: TodoListModel): Try[(TodoListModel, TodoListEvent)] = {
    val first :: second :: third :: rest = todoList.todos
    val result = todoList.copy(todos = first :: third :: second :: rest)
    val event = EscalatedEvent()
    Success((result, event))
  }

  def escalateCapability(todoListValue: TodoListModel): Try[() => Try[(TodoListModel, TodoListEvent)]] = Try {
    val isAbleToBeEscalated = isFull(todoListValue) && todoListValue.todos.slice(todoListValue.demarcationIndex, todoListValue.todos.size).nonEmpty
    if (!isAbleToBeEscalated) {
      throw new EscalateNotAllowException
    }
    () => escalate(todoListValue)
  }

  def unlockCapability(todoList: TodoListModel, unlockTime: Date): Try[Date => Try[(TodoListModel, TodoListEvent)]] = Try {
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

  private def isFull(todoList: TodoListModel): Boolean = {
    todoList.todos.slice(0, todoList.demarcationIndex).size >= MaxSize
  }

  private def isLocked(todoList: TodoListModel, unlockTime: Date) = {
    todoList.lastUnlockedAt.before(Date.from(unlockTime.toInstant.minusSeconds(1800)))
  }

  private def alreadyExists(todoList: TodoListModel, task: String): Boolean = {
    todoList.todos
      .map(todo => todo.getTask)
      .contains(task)
  }
}

