package com.doerapispring.storage

import java.util.Date

import com.doerapispring.domain._
import com.doerapispring.domain.events.TodoListEvent
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Repository

import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

@Repository
class TodoListModelEventSourcedRepository(private val todoListDao: TodoListDao,
                                          private val todoListEventStoreRepository: TodoListEventStoreRepository,
                                          private val objectMapper: ObjectMapper)
  extends OwnedObjectReadRepository[TodoListModel, UserId, ListId] {

  override def find(userId: UserId, listId: ListId): Option[TodoListModel] = {
    val todoListEntity: TodoListEntity = todoListDao.findByEmailAndListId(userId.get, listId.get)
    val todoListValue: TodoListModel = TodoListModel(listId, todoListEntity.name, List(), Date.from(todoListEntity.lastUnlockedAt.toInstant), todoListEntity.demarcationIndex)
    val eventStoreEntities: List[TodoListEventStoreEntity] = todoListEventStoreRepository.findAllByKeyUserIdAndKeyListIdOrderByKeyVersion(userId.get, listId.get).asScala.toList
    val events: List[TodoListEvent] = {
      eventStoreEntities.map(eventStoreEntity => objectMapper.readValue(eventStoreEntity.data, eventStoreEntity.eventClass))
    }
    events.foldLeft(Try(todoListValue)) {
      case (Success(todoList), event) => TodoListModel.applyEvent(todoList, event)
      case (Failure(exception), _) =>
        exception.printStackTrace()
        Failure(exception)
    }.toOption
  }
}
