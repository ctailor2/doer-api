package com.doerapispring.storage

import com.doerapispring.domain.events.TodoListEvent
import com.doerapispring.domain.{ListId, OwnedObjectWriteRepository, UserId}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.springframework.stereotype.Repository

import scala.jdk.CollectionConverters._

@Repository
class TodoListEventRepository(private val todoListDao: TodoListDao,
                              private val todoListEventStoreRepository: TodoListEventStoreRepository)
  extends OwnedObjectWriteRepository[TodoListEvent, UserId, ListId] {
  private val objectMapper: ObjectMapper = new ObjectMapper()
  objectMapper.registerModule(new DefaultScalaModule)

  override def save(userId: UserId, listId: ListId, todoListEvent: TodoListEvent): Unit = saveAll(userId, listId, List(todoListEvent))

  override def saveAll(userId: UserId, listId: ListId, todoListEvents: List[TodoListEvent]): Unit = {
    val nextVersion = todoListEventStoreRepository.findTopByKeyUserIdAndKeyListIdOrderByKeyVersionDesc(userId.get(), listId.get())
      .map(lastEventStoreEntity => lastEventStoreEntity.key.version + 1)
      .orElse(0)
    val eventStoreEntities = todoListEvents.zipWithIndex.map { case (todoListEvent, i) =>
      new TodoListEventStoreEntity(
        new TodoListEventStoreEntityKey(userId.get, listId.get, nextVersion + i),
        todoListEvent.getClass,
        objectMapper.writeValueAsString(todoListEvent))
    }
    todoListEventStoreRepository.saveAll(eventStoreEntities.asJava)
  }
}
