package com.doerapispring.messaging

import java.time.Clock

import com.doerapispring.domain._
import com.doerapispring.domain.events.TodoListEvent
import org.springframework.context.{ApplicationListener, PayloadApplicationEvent}
import org.springframework.stereotype.Component

@Component
class TodoListEventApplicationEventListener(private val todoListEventRepository: OwnedObjectWriteRepository[TodoListEvent, UserId, ListId],
                                            private val todoListModelSnapshotRepository: OwnedObjectWriteRepository[Snapshot[DeprecatedTodoListModel], UserId, ListId],
                                            private val clock: Clock)
  extends ApplicationListener[PayloadApplicationEvent[OwnedTodoListEvent]] {

  override def onApplicationEvent(event: PayloadApplicationEvent[OwnedTodoListEvent]): Unit = {
    val ownedTodoListEvent = event.getPayload
    todoListEventRepository.save(
      ownedTodoListEvent.userId, ownedTodoListEvent.listId, ownedTodoListEvent.todoListEvent)
  }
}
