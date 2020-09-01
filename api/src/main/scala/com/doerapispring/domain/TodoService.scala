package com.doerapispring.domain

import java.util.function.{BiFunction, Function, Supplier}

import com.doerapispring.domain.events.TodoListEvent
import org.springframework.stereotype.Service

import scala.util.Try

@Service
class TodoService(private val todoListRepository: OwnedObjectReadRepository[DeprecatedTodoListModel, UserId, ListId],
                  private val todoListEventRepository: OwnedObjectWriteRepository[TodoListEvent, UserId, ListId],
                  private val domainEventPublisher: DomainEventPublisher[DeprecatedTodoListModel, TodoListEvent, UserId, ListId],
                  private val todoRepository: IdentityGeneratingRepository[TodoId]) extends TodoApplicationService {

  override def performOperation(user: User,
                                listId: ListId,
                                eventProducer: Supplier[TodoListEvent],
                                operation: BiFunction[DeprecatedTodoListModel, TodoListEvent, Try[DeprecatedTodoListModel]]): Try[DeprecatedTodoListModel] = {
    val todoListEvent = eventProducer.get()
    Try(todoListRepository.find(user.getUserId, listId).get)
      .flatMap(todoList => {
        operation.apply(todoList, todoListEvent)
      })
      .map(todoList => domainEventPublisher.publish(todoList, todoListEvent, user.getUserId, listId))
  }

  override def performOperation(user: User,
                                listId: ListId,
                                eventProducer: Function[TodoId, TodoListEvent],
                                operation: BiFunction[DeprecatedTodoListModel, TodoListEvent, Try[DeprecatedTodoListModel]]): Try[DeprecatedTodoListModel] = {
    val todoId = todoRepository.nextIdentifier()
    val todoListEvent = eventProducer.apply(todoId)
    Try(todoListRepository.find(user.getUserId, listId).get)
      .flatMap(todoList => {
        operation.apply(todoList, todoListEvent)
      })
      .map(todoList => domainEventPublisher.publish(todoList, todoListEvent, user.getUserId, listId))
  }
}
