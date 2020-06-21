package com.doerapispring.domain

import java.util.function.{BiFunction, Function, Supplier}

import com.doerapispring.domain.events.TodoListEvent
import org.springframework.stereotype.Service

import scala.util.{Success, Try}

@Service
class TodoService(private val todoListRepository: OwnedObjectReadRepository[TodoListModel, UserId, ListId],
                  private val todoListEventRepository: OwnedObjectWriteRepository[TodoListEvent, UserId, ListId],
                  private val todoRepository: IdentityGeneratingRepository[TodoId]) extends TodoApplicationService {

  override def performOperation(user: User,
                                listId: ListId,
                                eventProducer: Supplier[TodoListEvent],
                                operation: BiFunction[TodoListModel, TodoListEvent, Try[TodoListModel]]): Try[TodoListModel] = {
    val todoListModel = Try(todoListRepository.find(user.getUserId, listId).get)
      .flatMap(todoList => operation.apply(todoList, eventProducer.get()))
    todoListModel match {
      case Success(_) => todoListEventRepository.save(user.getUserId, listId, eventProducer.get())
    }
    todoListModel
  }

  override def performOperation(user: User,
                                listId: ListId,
                                eventProducer: Function[TodoId, TodoListEvent],
                                operation: BiFunction[TodoListModel, TodoListEvent, Try[TodoListModel]]): Try[TodoListModel] = {
    val todoId = todoRepository.nextIdentifier()
    val todoListModel = Try(todoListRepository.find(user.getUserId, listId).get)
      .flatMap(todoList => {
        operation.apply(todoList, eventProducer.apply(todoId))
      })
    todoListModel match {
      case Success(_) => todoListEventRepository.save(user.getUserId, listId, eventProducer.apply(todoId))
    }
    todoListModel
  }
}
