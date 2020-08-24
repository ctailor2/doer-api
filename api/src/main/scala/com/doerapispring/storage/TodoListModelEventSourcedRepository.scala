package com.doerapispring.storage

import com.doerapispring.domain._
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Repository

import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

@Repository
class TodoListModelEventSourcedRepository(private val todoListDao: TodoListDao,
                                          private val todoListEventStoreRepository: TodoListEventStoreRepository,
                                          private val todoListModelSnapshotRepository: OwnedObjectReadRepository[TodoListModelSnapshot, UserId, ListId],
                                          private val objectMapper: ObjectMapper)
  extends OwnedObjectReadRepository[TodoListModel, UserId, ListId] {

  override def find(userId: UserId, listId: ListId): Option[TodoListModel] = {
    val todoListModelSnapshot = todoListModelSnapshotRepository.find(userId, listId)
    val events = todoListModelSnapshot
      .map(snapshot => snapshot.createdAt)
      .map(snapshotTime => todoListEventStoreRepository.findAllByKeyUserIdAndKeyListIdAndCreatedAtAfterOrderByKeyVersion(userId.get, listId.get, snapshotTime).asScala.toList)
      .map(eventStoreEntities => eventStoreEntities.map(eventStoreEntity => objectMapper.readValue(eventStoreEntity.data, eventStoreEntity.eventClass)))
      .getOrElse(List())
    events.foldLeft(Try(todoListModelSnapshot.get.todoListModel)) {
      case (Success(todoList), event) => TodoListModel.applyEvent(todoList, event)
      case (Failure(exception), _) =>
        exception.printStackTrace()
        Failure(exception)
    }.toOption
  }
}
