package com.doerapispring.domain

import com.doerapispring.domain.events.TodoListEvent
import org.springframework.stereotype.Service

@Service
class TodoService(private val todoListRepository: OwnedObjectReadRepository[TodoListModel, UserId, ListId],
                  private val todoListEventRepository: OwnedObjectWriteRepository[TodoListEvent, UserId, ListId],
                  private val domainEventPublisher: DomainEventPublisher[TodoListModel, TodoListEvent, UserId, ListId],
                  private val todoRepository: IdentityGeneratingRepository[TodoId]) extends TodoApplicationService {

  override def performOperation(user: User, listId: ListId, event: TodoListEvent): TodoListModel = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListModel.applyEvent(todoList, event))
      .map(todoList => domainEventPublisher.publish(todoList, event, user.getUserId, listId))
      .get
  }
}
