package com.doerapispring.domain

import com.doerapispring.domain.events.TodoListEvent
import org.springframework.stereotype.Service

import scala.util.{Failure, Success}

@Service
class TodoService(private val todoListRepository: OwnedObjectReadRepository[TodoListModel, UserId, ListId],
                  private val todoListEventRepository: OwnedObjectWriteRepository[TodoListEvent, UserId, ListId],
                  private val todoRepository: IdentityGeneratingRepository[TodoId]) extends TodoApplicationService {

  override def create(user: User, listId: ListId, task: String): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListModel.addCapability(todoList))
      .map {
        case Success(func) => func.apply(todoRepository.nextIdentifier(), task)
      }
      .foreach {
        case Success((_, event)) => todoListEventRepository.save(user.getUserId, listId, event)
        case Failure(_) =>
      }
  }

  override def createDeferred(user: User, listId: ListId, task: String): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListModel.addDeferred(todoList, todoRepository.nextIdentifier(), task))
      .foreach {
        case Success((_, event)) => todoListEventRepository.save(user.getUserId, listId, event)
        case Failure(_) =>
      }
  }

  override def delete(user: User, listId: ListId, todoId: TodoId): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListModel.delete(todoList, todoId))
      .foreach {
        case Success((_, event)) => todoListEventRepository.save(user.getUserId, listId, event)
        case Failure(_) =>
      }
  }

  override def displace(user: User, listId: ListId, task: String): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListModel.displaceCapability(todoList))
      .map {
        case Success(func) => func.apply(todoRepository.nextIdentifier(), task)
      }
      .foreach {
        case Success((_, event)) => todoListEventRepository.save(user.getUserId, listId, event)
      }
  }

  override def update(user: User, listId: ListId, todoId: TodoId, task: String): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListModel.update(todoList, todoId, task))
      .foreach {
        case Success((_, event)) => todoListEventRepository.save(user.getUserId, listId, event)
        case Failure(_) =>
      }
  }

  override def complete(user: User, listId: ListId, todoId: TodoId): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListModel.complete(todoList, todoId))
      .foreach {
        case Success((_, event)) => todoListEventRepository.save(user.getUserId, listId, event)
        case Failure(_) =>
      }
  }

  override def move(user: User, listId: ListId, todoId: TodoId, targetTodoId: TodoId): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListModel.move(todoList, todoId, targetTodoId))
      .foreach {
        case Success((_, event)) => todoListEventRepository.save(user.getUserId, listId, event)
        case Failure(_) =>
      }
  }

  override def pull(user: User, listId: ListId): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListModel.pullCapability(todoList))
      .map {
        case Success(func) => func.apply()
      }
      .foreach {
        case Success((_, event)) => todoListEventRepository.save(user.getUserId, listId, event)
        case Failure(_) =>
      }
  }

  override def escalate(user: User, listId: ListId): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListModel.escalate(todoList))
      .foreach {
        case Success((_, event)) => todoListEventRepository.save(user.getUserId, listId, event)
        case Failure(_) =>
      }
  }
}
