package com.doerapispring.domain

import com.doerapispring.domain.events.TodoListEvent
import org.springframework.stereotype.Service

import scala.util.{Failure, Success}

@Service
class TodoService(private val todoListRepository: OwnedObjectReadRepository[TodoListValue, UserId, ListId],
                  private val todoListEventRepository: OwnedObjectWriteRepository[TodoListEvent, UserId, ListId],
                  private val todoRepository: IdentityGeneratingRepository[TodoId]) extends TodoApplicationService {

  override def create(user: User, listId: ListId, task: String): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListValue.add(todoList, todoRepository.nextIdentifier(), task))
      .foreach {
        case Success((_, event)) => todoListEventRepository.save(user.getUserId, listId, event)
        case Failure(_) =>
      }
  }

  override def createDeferred(user: User, listId: ListId, task: String): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListValue.addDeferred(todoList, todoRepository.nextIdentifier(), task))
      .foreach {
        case Success((_, event)) => todoListEventRepository.save(user.getUserId, listId, event)
        case Failure(_) =>
      }
  }

  override def delete(user: User, listId: ListId, todoId: TodoId): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListValue.delete(todoList, todoId))
      .foreach {
        case Success((_, event)) => todoListEventRepository.save(user.getUserId, listId, event)
        case Failure(_) =>
      }
  }

  override def displace(user: User, listId: ListId, task: String): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListValue.displace(todoList, todoRepository.nextIdentifier(), task))
      .foreach {
        case Success((_, event)) => todoListEventRepository.save(user.getUserId, listId, event)
        case Failure(_) =>
      }
  }

  override def update(user: User, listId: ListId, todoId: TodoId, task: String): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListValue.update(todoList, todoId, task))
      .foreach {
        case Success((_, event)) => todoListEventRepository.save(user.getUserId, listId, event)
        case Failure(_) =>
      }
  }

  override def complete(user: User, listId: ListId, todoId: TodoId): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListValue.complete(todoList, todoId))
      .foreach {
        case Success((_, event)) => todoListEventRepository.save(user.getUserId, listId, event)
        case Failure(_) =>
      }
  }

  override def move(user: User, listId: ListId, todoId: TodoId, targetTodoId: TodoId): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListValue.move(todoList, todoId, targetTodoId))
      .foreach {
        case Success((_, event)) => todoListEventRepository.save(user.getUserId, listId, event)
        case Failure(_) =>
      }
  }

  override def pull(user: User, listId: ListId): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListValue.pull(todoList))
      .foreach {
        case Success((_, event)) => todoListEventRepository.save(user.getUserId, listId, event)
        case Failure(_) =>
      }
  }

  override def escalate(user: User, listId: ListId): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListValue.escalate(todoList))
      .foreach {
        case Success((_, event)) => todoListEventRepository.save(user.getUserId, listId, event)
        case Failure(_) =>
      }
  }
}
