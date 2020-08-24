package com.doerapispring.messaging

import com.doerapispring.domain.events.{DomainEvent, TodoListEvent}
import com.doerapispring.domain.{ListId, UserId}

case class OwnedTodoListEvent(userId: UserId, listId: ListId, todoListEvent: TodoListEvent) extends DomainEvent
