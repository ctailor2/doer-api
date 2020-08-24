package com.doerapispring.messaging

import com.doerapispring.domain.events.TodoListEvent
import com.doerapispring.domain.{DomainEventPublisher, ListId, TodoListModel, UserId}
import org.springframework.context.{ApplicationEventPublisher, PayloadApplicationEvent}
import org.springframework.stereotype.Component

@Component
class SpringApplicationEventsDomainEventPublisher(val applicationEventPublisher: ApplicationEventPublisher)
  extends DomainEventPublisher[TodoListModel, TodoListEvent, UserId, ListId] {

  override def publish(todoListModel: TodoListModel, todoListEvent: TodoListEvent, userId: UserId, listId: ListId): TodoListModel = {
    applicationEventPublisher.publishEvent(new  PayloadApplicationEvent[OwnedTodoListEvent](todoListModel, OwnedTodoListEvent(userId, listId, todoListEvent)))
    todoListModel
  }
}