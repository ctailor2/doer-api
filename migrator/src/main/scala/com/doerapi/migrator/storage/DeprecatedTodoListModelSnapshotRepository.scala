package com.doerapi.migrator.storage

import com.doerapi.migrator.domain.DeprecatedTodoListModel
import com.doerapispring.storage.AbstractTodoListModelSnapshotRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.jdbc.core.JdbcTemplate

class DeprecatedTodoListModelSnapshotRepository(jdbcTemplate: JdbcTemplate, objectMapper: ObjectMapper) extends AbstractTodoListModelSnapshotRepository[DeprecatedTodoListModel](classOf[DeprecatedTodoListModel], jdbcTemplate, objectMapper)
