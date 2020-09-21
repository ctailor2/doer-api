package com.doerapispring.domain

import java.util.{Calendar, Date, TimeZone}

import com.doerapispring.domain.events._

import scala.util.{Success, Try}

case class TodoListModel(todos: List[Todo] = List(),
                         completedTodos: List[CompletedTodo] = List(),
                         lastUnlockedAt: Date = new Date(0L),
                         demarcationIndex: Integer = 0)

case class TodoListModelCapabilities(add: Option[String => TodoAddedEvent],
                                     displace: Option[String => TodoDisplacedEvent],
                                     addDeferred: String => DeferredTodoAddedEvent,
                                     escalate: Option[EscalatedEvent],
                                     pull: Option[PulledEvent],
                                     unlock: Option[Date => UnlockedEvent],
                                     todoCapabilities: List[TodoCapabilities],
                                     deferredTodoCapabilities: List[TodoCapabilities])

case class TodoCapabilities(update: String => TodoUpdatedEvent,
                            complete: Date => TodoCompletedEvent,
                            delete: TodoDeletedEvent,
                            move: List[TodoMovedEvent])

object TodoListModel {
  val MaxSize: Int = 2

  def capabilities(todoList: TodoListModel, now: Date): TodoListModelCapabilities = {
    TodoListModelCapabilities(
      add = Option.unless(isFull(todoList))((task: String) => TodoAddedEvent(task)),
      displace = Option.when(isFull(todoList))((task: String) => TodoDisplacedEvent(task)),
      addDeferred = (task: String) => DeferredTodoAddedEvent(task),
      escalate = Option.when(isAbleToBeEscalated(todoList))(EscalatedEvent()),
      pull = Option.when(isAbleToBePulled(todoList))(PulledEvent()),
      unlock = Option.when(isAbleToBeUnlocked(todoList, now))((unlockedAt: Date) => UnlockedEvent(unlockedAt)),
      todoCapabilities = getTodos(todoList).map(todo => {
        val index = todoList.todos.indexOf(todo)
        TodoCapabilities(
          update = (task: String) => TodoUpdatedEvent(index, task),
          complete = (completedAt: Date) => TodoCompletedEvent(index, completedAt),
          delete = TodoDeletedEvent(index),
          move = todoList.todos.indices
            .map(index => TodoMovedEvent(todoList.todos.indexOf(todo), index))
            .toList)
      }),
      deferredTodoCapabilities = getDeferredTodos(todoList, now).map(todo => {
        val index = todoList.todos.indexOf(todo)
        TodoCapabilities(
          update = (task: String) => TodoUpdatedEvent(index, task),
          complete = (completedAt: Date) => TodoCompletedEvent(index, completedAt),
          delete = TodoDeletedEvent(index),
          move = todoList.todos.indices
            .map(index => TodoMovedEvent(todoList.todos.indexOf(todo), index))
            .toList)
      })
    )
  }

  //  TODO: From the controller, produce the TodoListEvent from the capabilities object
  //  so this method doesn't have to guard against the capabilities that are not
  //  always possible - those where the capability is optional (of Option type)
  def applyEvent(todoList: TodoListModel, todoListEvent: TodoListEvent): TodoListModel = {
    todoListEvent match {
      case TodoUpdatedEvent(index, task) => update(todoList, index, task)
      case TodoCompletedEvent(index, completedAt) => complete(todoList, index, completedAt)
      case TodoDisplacedEvent(task) => displace(todoList, task)
      case TodoDeletedEvent(index) => delete(todoList, index)
      case TodoAddedEvent(task) => add(todoList, task)
      case EscalatedEvent() => escalate(todoList)
      case DeferredTodoAddedEvent(task) => addDeferred(todoList, task)
      case TodoMovedEvent(index, targetIndex) => move(todoList, index, targetIndex)
      case PulledEvent() => pull(todoList)
      case UnlockedEvent(unlockedAt) => unlock(todoList, unlockedAt)
      case _ => todoList
    }
  }

  def add(todoList: TodoListModel, task: String): TodoListModel = {
    val todo = Todo(task)
    todoList.copy(todos = todo +: todoList.todos, demarcationIndex = todoList.demarcationIndex + 1)
  }

  def addDeferred(todoList: TodoListModel, task: String): TodoListModel = {
    val todo = Todo(task)
    todoList.copy(todos = todoList.todos :+ todo)
  }

  def delete(todoList: TodoListModel, index: Int): TodoListModel = {
    val newDemarcationIndex: Int = if (index < todoList.demarcationIndex) todoList.demarcationIndex - 1 else todoList.demarcationIndex
    todoList.copy(todos = todoList.todos.take(index) ++ todoList.todos.drop(index + 1), demarcationIndex = newDemarcationIndex)
  }

  def displace(todoList: TodoListModel, task: String): TodoListModel = {
    val todo = Todo(task)
    todoList.copy(todos = todo +: todoList.todos)
  }

  def update(todoList: TodoListModel, index: Int, task: String): TodoListModel = {
    val todo: Todo = todoList.todos(index)
    val updatedTodo = todo.copy(task = task)
    todoList.copy(todos = todoList.todos.updated(index, updatedTodo))
  }

  def complete(todoList: TodoListModel, index: Int, completedAt: Date): TodoListModel = {
    val newDemarcationIndex: Int = if (index < todoList.demarcationIndex) todoList.demarcationIndex - 1 else todoList.demarcationIndex
    val todo = todoList.todos(index)
    todoList.copy(
      todos = todoList.todos.take(index) ++ todoList.todos.drop(index + 1),
      completedTodos = CompletedTodo(todo.task, completedAt) +: todoList.completedTodos,
      demarcationIndex = newDemarcationIndex)
  }

  def move(todoList: TodoListModel, index: Int, targetIndex: Int): TodoListModel = {
    val sourceTodo: Todo = todoList.todos(index)
    val targetTodo: Todo = todoList.todos(targetIndex)
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

  def getTodos(todoList: TodoListModel): List[Todo] = {
    todoList.todos.slice(0, todoList.demarcationIndex)
  }

  def getDeferredTodos(todoList: TodoListModel, unlockTime: Date): List[Todo] = {
    if (isLocked(todoList, unlockTime)) List.empty else todoList.todos.slice(todoList.demarcationIndex, todoList.todos.size)
  }

  def unlock(todoList: TodoListModel, unlockTime: Date): TodoListModel = {
    todoList.copy(lastUnlockedAt = unlockTime)
  }

  def unlockDurationMs(todoList: TodoListModel, compareTime: Date): Long = {
    val unlockDuration = 1800000L
    val duration = todoList.lastUnlockedAt.toInstant.toEpochMilli + unlockDuration - compareTime.toInstant.toEpochMilli
    if (duration > 0) duration else 0L
  }

  def pull(todoList: TodoListModel): TodoListModel = {
    todoList.copy(demarcationIndex = Math.min(todoList.todos.size, MaxSize))
  }

  private def isAbleToBePulled(todoList: TodoListModel) = {
    !isFull(todoList) && todoList.todos.slice(todoList.demarcationIndex, todoList.todos.size).nonEmpty
  }

  def escalate(todoList: TodoListModel): TodoListModel = {
    val first :: second :: third :: rest = todoList.todos
    todoList.copy(todos = first :: third :: second :: rest)
  }

  private def isAbleToBeEscalated(todoListValue: TodoListModel): Boolean = {
    isFull(todoListValue) && todoListValue.todos.slice(todoListValue.demarcationIndex, todoListValue.todos.size).nonEmpty
  }

  private def isAbleToBeUnlocked(todoList: TodoListModel, unlockTime: Date) = {
    val calendar = Calendar.getInstance
    calendar.setTimeZone(TimeZone.getTimeZone("UTC"))
    calendar.setTime(unlockTime)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val isAbleToBeUnlocked = isLocked(todoList, unlockTime) && todoList.lastUnlockedAt.before(calendar.getTime)
    isAbleToBeUnlocked
  }

  private def isFull(todoList: TodoListModel): Boolean = {
    todoList.todos.slice(0, todoList.demarcationIndex).size >= MaxSize
  }

  private def isLocked(todoList: TodoListModel, unlockTime: Date) = {
    todoList.lastUnlockedAt.before(Date.from(unlockTime.toInstant.minusSeconds(1800)))
  }

  private def alreadyExists(todoList: TodoListModel, task: String): Boolean = {
    todoList.todos
      .map(todo => todo.task)
      .contains(task)
  }
}

