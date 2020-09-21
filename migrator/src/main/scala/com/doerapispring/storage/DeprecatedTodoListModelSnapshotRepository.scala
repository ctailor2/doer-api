package com.doerapispring.storage

import com.doerapispring.domain.DeprecatedTodoListModel
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class DeprecatedTodoListModelSnapshotRepository(jdbcTemplate: JdbcTemplate, objectMapper: ObjectMapper) extends AbstractTodoListModelSnapshotRepository[DeprecatedTodoListModel](classOf[DeprecatedTodoListModel], jdbcTemplate, objectMapper)
