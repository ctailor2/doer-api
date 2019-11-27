package com.doerapispring.storage;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface CompletedTodoDAO extends CrudRepository<CompletedTodoEntity, String> {
    @Query("SELECT uuid, user_id, task, completed_at, list_id " +
        "FROM completed_todos " +
        "WHERE user_id = :userId " +
        "AND list_id = :listId " +
        "ORDER BY completed_at DESC")
    List<CompletedTodoEntity> findByUserIdAndListIdOrderByCompletedAtDesc(
        @Param("userId") Long userId,
        @Param("listId") String listId);
}
