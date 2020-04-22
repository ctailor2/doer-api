package com.doerapispring.domain

import java.time.Clock
import java.util.{Calendar, Date, TimeZone}

case class TodoListValue(clock: Clock,
                         userId: UserId,
                         todos: List[Todo],
                         listId: ListId,
                         name: String,
                         lastUnlockedAt: Date,
                         demarcationIndex: Integer)

object TodoListValue {
  val MaxSize: Int = 2

  def add(todoList: TodoListValue, todoId: TodoId, task: String): TodoListValue = {
    if (alreadyExists(todoList, task)) {
      throw new DuplicateTodoException
    }
    if (isFull(todoList)) {
      throw new ListSizeExceededException
    }
    val todo = new Todo(todoId, task)
    todoList.copy(
      todos = todo +: todoList.todos,
      demarcationIndex = todoList.demarcationIndex + 1)
  }

  def addDeferred(todoList: TodoListValue, todoId: TodoId, task: String): TodoListValue = {
    if (alreadyExists(todoList, task)) {
      throw new DuplicateTodoException
    }
    val todo = new Todo(todoId, task)
    todoList.copy(todos = todoList.todos :+ todo)
  }

  def delete(todoList: TodoListValue, todoId: TodoId): TodoListValue = {
    val todoMatches: Todo => Boolean = todo => todo.getTodoId.equals(todoId)
    if (!todoList.todos.exists(todoMatches)) throw new TodoNotFoundException
    val indexOfTodo = todoList.todos.indexWhere(todoMatches)
    val newDemarcationIndex: Int = if (indexOfTodo < todoList.demarcationIndex) todoList.demarcationIndex - 1 else todoList.demarcationIndex
    todoList.copy(todos = todoList.todos.filterNot(todoMatches), demarcationIndex = newDemarcationIndex)
  }

  def displace(todoList: TodoListValue, todoId: TodoId, task: String): TodoListValue = {
    if (!isFull(todoList)) throw new ListNotFullException
    if (alreadyExists(todoList, task)) throw new DuplicateTodoException
    val todo = new Todo(todoId, task)
    todoList.copy(todos = todo +: todoList.todos)
  }

  def update(todoList: TodoListValue, todoId: TodoId, task: String): TodoListValue = {
    if (alreadyExists(todoList, task)) throw new DuplicateTodoException
    val todoMatches: Todo => Boolean = todo => todo.getTodoId.equals(todoId)
    val todo: Todo = todoList.todos.find(todoMatches).getOrElse(throw new TodoNotFoundException)
    todo.setTask(task)
    todoList.copy(todos = todoList.todos)
  }

  def complete(todoList: TodoListValue, todoId: TodoId): TodoListValue = delete(todoList, todoId)

  def move(todoList: TodoListValue, todoId: TodoId, targetTodoId: TodoId): TodoListValue = {
    val sourceTodoMatches: Todo => Boolean = todo => todo.getTodoId.equals(todoId)
    val targetTodoMatches: Todo => Boolean = todo => todo.getTodoId.equals(targetTodoId)
    val sourceTodo: Todo = todoList.todos.find(sourceTodoMatches).getOrElse(throw new TodoNotFoundException)
    val targetTodo: Todo = todoList.todos.find(targetTodoMatches).getOrElse(throw new TodoNotFoundException)
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

  def getTodos(todoList: TodoListValue): List[Todo] = {
    todoList.todos.slice(0, todoList.demarcationIndex)
  }

  def getDeferredTodos(todoList: TodoListValue): List[Todo] = {
    if (isLocked(todoList)) List.empty else todoList.todos.slice(todoList.demarcationIndex, todoList.todos.size)
  }

  def unlock(todoList: TodoListValue): TodoListValue = {
    if (!isAbleToBeUnlocked(todoList)) throw new LockTimerNotExpiredException
    todoList.copy(lastUnlockedAt = Date.from(todoList.clock.instant()))
  }

  def pull(todoList: TodoListValue): TodoListValue = {
    todoList.copy(demarcationIndex = Math.min(todoList.todos.size, MaxSize))
  }

  def escalate(todoList: TodoListValue): TodoListValue = {
    if (!isFull(todoList) || todoList.todos.slice(todoList.demarcationIndex, todoList.todos.size).isEmpty) {
      throw new EscalateNotAllowException
    }
    val first :: second :: third :: rest = todoList.todos
    todoList.copy(todos = first :: third :: second :: rest)
  }

  def isAbleToBeUnlocked(todoList: TodoListValue): Boolean = {
    val now = Date.from(todoList.clock.instant)
    val calendar = Calendar.getInstance
    calendar.setTimeZone(TimeZone.getTimeZone(todoList.clock.getZone))
    calendar.setTime(now)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val beginningOfToday = calendar.getTime
    isLocked(todoList) && todoList.lastUnlockedAt.before(beginningOfToday)
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

  private def isLocked(todoList: TodoListValue) = {
    todoList.lastUnlockedAt.before(Date.from(todoList.clock.instant().minusSeconds(1800L)))
  }

  private def alreadyExists(todoList: TodoListValue, task: String): Boolean = {
    todoList.todos
      .map(todo => todo.getTask)
      .contains(task)
  }
}

