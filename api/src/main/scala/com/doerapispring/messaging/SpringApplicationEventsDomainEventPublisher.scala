package com.doerapispring.messaging

import com.doerapispring.domain.events.TodoListEvent
import com.doerapispring.domain.{DeprecatedTodoListModel, DomainEventPublisher, ListId, UserId}
import org.springframework.context.{ApplicationEventPublisher, PayloadApplicationEvent}
import org.springframework.stereotype.Component

@Component
class SpringApplicationEventsDomainEventPublisher(val applicationEventPublisher: ApplicationEventPublisher)
  extends DomainEventPublisher[DeprecatedTodoListModel, TodoListEvent, UserId, ListId] {

  override def publish(todoListModel: DeprecatedTodoListModel, todoListEvent: TodoListEvent, userId: UserId, listId: ListId): DeprecatedTodoListModel = {
    applicationEventPublisher.publishEvent(new  PayloadApplicationEvent[OwnedTodoListEvent](todoListModel, OwnedTodoListEvent(userId, listId, todoListEvent)))
    todoListModel
  }
}