package com.doerapispring.storage

import java.sql.ResultSet
import java.util.Date

import com.doerapispring.domain._
import com.doerapispring.domain.events._
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.jdbc.core.namedparam.{MapSqlParameterSource, NamedParameterJdbcTemplate}
import org.springframework.jdbc.core.{JdbcTemplate, RowMapper}
import org.springframework.stereotype.Repository

import scala.jdk.CollectionConverters._

@Repository
class CompletedTodoListEventSourcedRepository(val objectMapper: ObjectMapper,
                                              val jdbcTemplate: JdbcTemplate)
  extends OwnedObjectReadRepository[CompletedTodoList, UserId, ListId] {

  override def find(userId: UserId, listId: ListId): Option[CompletedTodoList] = {
    val namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate)
    val parameters = new MapSqlParameterSource("userId", userId.get)
      .addValue("listId", listId.get)
      .addValue("eventClasses", List(
        classOf[TodoAddedEvent].getName,
        classOf[DeferredTodoAddedEvent].getName,
        classOf[TodoCompletedEvent].getName,
        classOf[TodoDisplacedEvent].getName).asJava)
    val rowMapper = new RowMapper[(String, String, Date)] {
      override def mapRow(rs: ResultSet, rowNum: Int): (String, String, Date) = {
        (rs.getString("data"), rs.getString("event_class"), Date.from(rs.getTimestamp("created_at").toInstant))
      }
    }
    val timestampedDomainEvents = namedParameterJdbcTemplate.query(
      "SELECT data, event_class, created_at " +
        "FROM list_events " +
        "WHERE user_id = :userId AND list_id = :listId AND event_class IN (:eventClasses) " +
        "ORDER BY version ASC",
      parameters,
      rowMapper).asScala.toList
      .map { case (data, eventClass, createdAt) =>
        TimestampedDomainEvent(objectMapper.readValue(data, Class.forName(eventClass).asSubclass(classOf[DomainEvent])), createdAt)
      }.asJava
    Option.apply(new CompletedTodoList(userId, listId).withEvents(timestampedDomainEvents))
  }
}