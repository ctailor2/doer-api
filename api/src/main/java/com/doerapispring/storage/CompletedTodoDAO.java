package com.doerapispring.storage;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface CompletedTodoDAO extends CrudRepository<CompletedTodoEntity, String> {
    @Query("SELECT uuid, user_identifier, task, completed_at, list_id " +
        "FROM completed_todos " +
        "WHERE user_identifier = :userIdentifier " +
        "AND list_id = :listId " +
        "ORDER BY completed_at DESC")
    List<CompletedTodoEntity> findByUserIdAndListIdOrderByCompletedAtDesc(
        @Param("userIdentifier") String userIdentifier,
        @Param("listId") String listId);
}
