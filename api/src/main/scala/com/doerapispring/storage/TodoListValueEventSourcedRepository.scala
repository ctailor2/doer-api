package com.doerapispring.storage

import java.util.Date

import com.doerapispring.domain._
import com.doerapispring.domain.events.TodoListEvent
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Repository

import scala.jdk.CollectionConverters._

@Repository
class TodoListValueEventSourcedRepository(private val todoListDao: TodoListDao,
                                          private val todoListEventStoreRepository: TodoListEventStoreRepository,
                                          private val objectMapper: ObjectMapper)
  extends OwnedObjectReadRepository[TodoListValue, UserId, ListId] {

  override def find(userId: UserId, listId: ListId): Option[TodoListValue] = {
    val todoListEntity: TodoListEntity = todoListDao.findByEmailAndListId(userId.get, listId.get)
    val todoListValue: TodoListValue = TodoListValue(List(), todoListEntity.name, Date.from(todoListEntity.lastUnlockedAt.toInstant), todoListEntity.demarcationIndex)
    val eventStoreEntities: List[TodoListEventStoreEntity] = todoListEventStoreRepository.findAllByKeyUserIdAndKeyListIdOrderByKeyVersion(userId.get, listId.get).asScala.toList
    val events: List[TodoListEvent] = {
      eventStoreEntities.map(eventStoreEntity => objectMapper.readValue(eventStoreEntity.data, eventStoreEntity.eventClass))
    }
    Option.apply(events.foldLeft(todoListValue)((todoList, event) => TodoListValue.applyEvent(todoList, event)))
  }
}
