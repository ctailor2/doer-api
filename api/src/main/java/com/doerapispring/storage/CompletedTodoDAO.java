package com.doerapispring.storage;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface CompletedTodoDAO extends CrudRepository<CompletedTodoEntity, String> {
    @Query("SELECT uuid, user_id, task, completed_at, list_id " +
        "FROM completed_todos " +
        "JOIN users ON users.id = completed_todos.user_id " +
        "WHERE users.email = :email " +
        "AND list_id = :listId " +
        "ORDER BY completed_at DESC")
    List<CompletedTodoEntity> findByUserIdOrderByCompletedAtDesc(
        @Param("email") String email,
        @Param("listId") String listId);
}
