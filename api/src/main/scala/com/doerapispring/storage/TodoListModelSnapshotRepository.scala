package com.doerapispring.storage

import com.doerapispring.domain.TodoListModel
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class TodoListModelSnapshotRepository(jdbcTemplate: JdbcTemplate, objectMapper: ObjectMapper) extends AbstractTodoListModelSnapshotRepository[TodoListModel](classOf[TodoListModel], jdbcTemplate, objectMapper)
