package com.doerapispring.storage

import java.sql.ResultSet

import com.doerapispring.domain._
import com.doerapispring.domain.events.TodoListEvent
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.jdbc.core.{JdbcTemplate, RowMapper}
import org.springframework.stereotype.Repository

import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

@Repository
class TodoListModelEventSourcedRepository(private val todoListModelSnapshotRepository: OwnedObjectReadRepository[TodoListModelSnapshot, UserId, ListId],
                                          private val objectMapper: ObjectMapper,
                                          private val jdbcTemplate: JdbcTemplate)
  extends OwnedObjectReadRepository[TodoListModel, UserId, ListId] {

  override def find(userId: UserId, listId: ListId): Option[TodoListModel] = {
    val todoListModelSnapshot = todoListModelSnapshotRepository.find(userId, listId)
    val events = todoListModelSnapshot
      .map(snapshot => snapshot.createdAt)
      .toList
      .flatMap(snapshotTime => {
        val queryArguments: Array[AnyRef] = List(userId.get, listId.get, snapshotTime).toArray
        val rowMapper = new RowMapper[(String, String)] {
          override def mapRow(rs: ResultSet, rowNum: Int): (String, String) = {
            (rs.getString("data"), rs.getString("event_class"))
          }
        }
        jdbcTemplate.query(
          "SELECT data, event_class " +
            "FROM list_events " +
            "WHERE user_id = ? AND list_id = ? AND created_at > ? " +
            "ORDER BY version ASC", queryArguments, rowMapper).asScala.toList
      })
      .map { case (data, eventClass) =>
        objectMapper.readValue(data, Class.forName(eventClass).asSubclass(classOf[TodoListEvent]))
      }
    events.foldLeft(Try(todoListModelSnapshot.get.todoListModel)) {
      case (Success(todoList), event) => TodoListModel.applyEvent(todoList, event)
      case (Failure(exception), _) =>
        exception.printStackTrace()
        Failure(exception)
    }.toOption
  }
}
