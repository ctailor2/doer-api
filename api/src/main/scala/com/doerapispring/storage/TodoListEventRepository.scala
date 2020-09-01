package com.doerapispring.storage

import java.sql.Timestamp
import java.time.Clock

import com.doerapispring.domain.events.TodoListEvent
import com.doerapispring.domain.{ListId, OwnedObjectWriteRepository, UserId}
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.{MapSqlParameterSource, SqlParameterSource}
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository

@Repository
class TodoListEventRepository(private val objectMapper: ObjectMapper,
                              private val jdbcTemplate: JdbcTemplate,
                              private val clock: Clock)
  extends OwnedObjectWriteRepository[TodoListEvent, UserId, ListId] {

  override def save(userId: UserId, listId: ListId, todoListEvent: TodoListEvent): Unit = saveAll(userId, listId, List(todoListEvent))

  override def saveAll(userId: UserId, listId: ListId, todoListEvents: List[TodoListEvent]): Unit = {
    val insert = new SimpleJdbcInsert(jdbcTemplate).withTableName("list_events");
    val queryArguments: Array[AnyRef] = List(userId.get, listId.get).toArray
    val nextVersion = jdbcTemplate.queryForList(
      "SELECT version " +
        "FROM list_events " +
        "WHERE user_id = ? " +
        "AND list_id = ? " +
        "ORDER BY version DESC " +
        "LIMIT 1", queryArguments, classOf[Int])
      .stream()
      .findFirst()
      .map(lastVersion => lastVersion + 1)
      .orElse(0)

    val sqlParameterSources: Array[SqlParameterSource] = todoListEvents.zipWithIndex.map { case (todoListEvent, i) =>
      new MapSqlParameterSource("user_id", userId.get)
        .addValue("list_id", listId.get)
        .addValue("version", nextVersion + i)
        .addValue("event_class", todoListEvent.getClass.getName)
        .addValue("data", objectMapper.writeValueAsString(todoListEvent))
        .addValue("created_at", Timestamp.from(clock.instant()))
    }.toArray
    insert.executeBatch(sqlParameterSources: _*)
  }
}
