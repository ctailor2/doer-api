package com.doerapispring.storage

import java.util.{Date, Optional}

import com.doerapispring.domain._
import com.doerapispring.domain.events.TodoListEvent
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.jdk.CollectionConverters._

class TodoListValueEventSourcedRepository(private val todoListDao: TodoListDao,
                                          private val todoListEventStoreRepository: TodoListEventStoreRepository,
                                          private val objectMapper: ObjectMapper = new ObjectMapper().registerModule(new DefaultScalaModule)) extends OwnedObjectRepository[TodoListValue, UserId, ListId] {

  override def find(userId: UserId, listId: ListId): Optional[TodoListValue] = {
    val todoListEntity: TodoListEntity = todoListDao.findByEmailAndListId(userId.get, listId.get)
    val todoListValue: TodoListValue = TodoListValue(List(), todoListEntity.name, Date.from(todoListEntity.lastUnlockedAt.toInstant), todoListEntity.demarcationIndex)
    val eventStoreEntities: List[TodoListEventStoreEntity] = todoListEventStoreRepository.findAllByKeyUserIdAndKeyListIdOrderByKeyVersion(userId.get, listId.get).asScala.toList
    val events: List[TodoListEvent] = {
      eventStoreEntities.map(eventStoreEntity => objectMapper.readValue(eventStoreEntity.data, eventStoreEntity.eventClass))
    }
    Optional.of(events.foldLeft(todoListValue)((todoList, event) => TodoListValue.applyEvent(todoList, event)))
  }

  override def save(model: TodoListValue): Unit = ???

  override def nextIdentifier(): ListId = ???
}
