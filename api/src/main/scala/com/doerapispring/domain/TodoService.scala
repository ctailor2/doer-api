package com.doerapispring.domain

import java.util.function.{BiFunction, Function}

import com.doerapispring.domain.events.TodoListEvent
import org.springframework.stereotype.Service

import scala.util.{Failure, Success, Try}

@Service
class TodoService(private val todoListRepository: OwnedObjectReadRepository[TodoListModel, UserId, ListId],
                  private val todoListEventRepository: OwnedObjectWriteRepository[TodoListEvent, UserId, ListId],
                  private val todoRepository: IdentityGeneratingRepository[TodoId]) extends TodoApplicationService {

  override def performOperation(user: User, listId: ListId, operation: BiFunction[TodoListModel, TodoId, Try[(TodoListModel, TodoListEvent)]]): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => (todoList, todoRepository.nextIdentifier()))
      .map { case (todoList, todoId) => operation.apply(todoList, todoId) }
      .foreach {
        case Success((_, event)) => todoListEventRepository.save(user.getUserId, listId, event)
        case Failure(_) =>
      }
  }

  override def performOperation(user: User, listId: ListId, operation: Function[TodoListModel, Try[(TodoListModel, TodoListEvent)]]): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => operation.apply(todoList))
      .foreach {
        case Success((_, event)) => todoListEventRepository.save(user.getUserId, listId, event)
        case Failure(_) =>
      }
  }
}
