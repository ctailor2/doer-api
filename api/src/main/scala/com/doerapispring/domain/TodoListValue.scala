package com.doerapispring.domain

import java.util.{Calendar, Date, TimeZone}

import com.doerapispring.domain.events._

case class TodoListValue(todos: List[Todo],
                         lastUnlockedAt: Date,
                         demarcationIndex: Integer)

object TodoListValue {
  val MaxSize: Int = 2

  def applyEvent(todoList: TodoListValue, todoListEvent: TodoListEvent): TodoListValue = {
    todoListEvent match {
      case TodoUpdatedEvent(todoId, task) => update(todoList, new TodoId(todoId), task)._1
      case TodoCompletedEvent(completedTodoId) => complete(todoList, new TodoId(completedTodoId))._1
      case TodoDisplacedEvent(todoId, task) => displace(todoList, new TodoId(todoId), task)._1
      case TodoDeletedEvent(todoId) => delete(todoList, new TodoId(todoId))._1
      case TodoAddedEvent(todoId, task) => add(todoList, new TodoId(todoId), task)._1
      case EscalatedEvent() => escalate(todoList)._1
      case DeferredTodoAddedEvent(todoId, task) => addDeferred(todoList, new TodoId(todoId), task)._1
      case TodoMovedEvent(todoId, targetTodoId) => move(todoList, new TodoId(todoId), new TodoId(targetTodoId))._1
      case PulledEvent() => pull(todoList)._1
      case UnlockedEvent(unlockedAt) => unlock(todoList, unlockedAt)._1
    }
  }

  def add(todoList: TodoListValue, todoId: TodoId, task: String): (TodoListValue, TodoListEvent) = {
    if (alreadyExists(todoList, task)) {
      throw new DuplicateTodoException
    }
    if (isFull(todoList)) {
      throw new ListSizeExceededException
    }
    val todo = new Todo(todoId, task)
    val result = todoList.copy(todos = todo +: todoList.todos, demarcationIndex = todoList.demarcationIndex + 1)
    val event = TodoAddedEvent(todoId.getIdentifier, task)
    (result, event)
  }

  def addDeferred(todoList: TodoListValue, todoId: TodoId, task: String): (TodoListValue, TodoListEvent) = {
    if (alreadyExists(todoList, task)) {
      throw new DuplicateTodoException
    }
    val todo = new Todo(todoId, task)
    val result = todoList.copy(todos = todoList.todos :+ todo)
    val event = DeferredTodoAddedEvent(todoId.getIdentifier, task)
    (result, event)
  }

  def delete(todoList: TodoListValue, todoId: TodoId): (TodoListValue, TodoListEvent) = {
    val todoMatches: Todo => Boolean = todo => todo.getTodoId.equals(todoId)
    if (!todoList.todos.exists(todoMatches)) throw new TodoNotFoundException
    val indexOfTodo = todoList.todos.indexWhere(todoMatches)
    val newDemarcationIndex: Int = if (indexOfTodo < todoList.demarcationIndex) todoList.demarcationIndex - 1 else todoList.demarcationIndex
    val result = todoList.copy(todos = todoList.todos.filterNot(todoMatches), demarcationIndex = newDemarcationIndex)
    val event = TodoDeletedEvent(todoId.getIdentifier)
    (result, event)
  }

  def displace(todoList: TodoListValue, todoId: TodoId, task: String): (TodoListValue, TodoListEvent) = {
    if (!isFull(todoList)) throw new ListNotFullException
    if (alreadyExists(todoList, task)) throw new DuplicateTodoException
    val todo = new Todo(todoId, task)
    val result = todoList.copy(todos = todo +: todoList.todos)
    val event = TodoDisplacedEvent(todoId.getIdentifier, task)
    (result, event)
  }

  def update(todoList: TodoListValue, todoId: TodoId, task: String): (TodoListValue, TodoListEvent) = {
    if (alreadyExists(todoList, task)) throw new DuplicateTodoException
    val todoMatches: Todo => Boolean = todo => todo.getTodoId.equals(todoId)
    val todo: Todo = todoList.todos.find(todoMatches).getOrElse(throw new TodoNotFoundException)
    todo.setTask(task)
    val result = todoList.copy(todos = todoList.todos)
    val event = TodoUpdatedEvent(todoId.getIdentifier, task)
    (result, event)
  }

  def complete(todoList: TodoListValue, todoId: TodoId): (TodoListValue, TodoListEvent) = {
    val result = delete(todoList, todoId)._1
    val event = TodoCompletedEvent(todoId.getIdentifier)
    (result, event)
  }

  def move(todoList: TodoListValue, todoId: TodoId, targetTodoId: TodoId): (TodoListValue, TodoListEvent) = {
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

  def getTodos(todoList: TodoListValue): List[Todo] = {
    todoList.todos.slice(0, todoList.demarcationIndex)
  }

  def getDeferredTodos(todoList: TodoListValue, unlockTime: Date): List[Todo] = {
    if (isLocked(todoList, unlockTime)) List.empty else todoList.todos.slice(todoList.demarcationIndex, todoList.todos.size)
  }

  def unlock(todoList: TodoListValue, unlockTime: Date): (TodoListValue, TodoListEvent) = {
    if (!isAbleToBeUnlocked(todoList, unlockTime)) throw new LockTimerNotExpiredException
    val result = todoList.copy(lastUnlockedAt = unlockTime)
    val event = UnlockedEvent(unlockTime)
    (result, event)
  }

  def pull(todoList: TodoListValue): (TodoListValue, TodoListEvent) = {
    val result = todoList.copy(demarcationIndex = Math.min(todoList.todos.size, MaxSize))
    val event = PulledEvent()
    (result, event)
  }

  def escalate(todoList: TodoListValue): (TodoListValue, TodoListEvent) = {
    if (!isAbleToBeEscalated(todoList)) {
      throw new EscalateNotAllowException
    }
    val first :: second :: third :: rest = todoList.todos
    val result = todoList.copy(todos = first :: third :: second :: rest)
    val event = EscalatedEvent()
    (result, event)
  }

  def isAbleToBeUnlocked(todoList: TodoListValue, unlockTime: Date): Boolean = {
    val calendar = Calendar.getInstance
    calendar.setTimeZone(TimeZone.getTimeZone("UTC"))
    calendar.setTime(unlockTime)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    isLocked(todoList, unlockTime) && todoList.lastUnlockedAt.before(calendar.getTime)
  }

  def isAbleToBeReplenished(todoList: TodoListValue): Boolean = {
    !isFull(todoList) && todoList.todos.slice(todoList.demarcationIndex, todoList.todos.size).nonEmpty
  }

  def isFull(todoList: TodoListValue): Boolean = {
    todoList.todos.slice(0, todoList.demarcationIndex).size >= MaxSize
  }

  def isAbleToBeEscalated(todoListValue: TodoListValue): Boolean = {
    isFull(todoListValue) && todoListValue.todos.slice(todoListValue.demarcationIndex, todoListValue.todos.size).nonEmpty
  }

  private def isLocked(todoList: TodoListValue, unlockTime: Date) = {
    todoList.lastUnlockedAt.before(Date.from(unlockTime.toInstant.minusSeconds(1800)))
  }

  private def alreadyExists(todoList: TodoListValue, task: String): Boolean = {
    todoList.todos
      .map(todo => todo.getTask)
      .contains(task)
  }
}

