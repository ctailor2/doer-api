package com.doerapispring.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

interface TodoListDao extends JpaRepository<TodoListEntity, Long> {
    @Query("SELECT tl FROM TodoListEntity tl " +
        "LEFT JOIN FETCH tl.todoEntities t " +
        "WHERE tl.userEntity.email = ?1 " +
        "ORDER BY t.position")
    List<TodoListEntity> findByEmail(String email);

    @Query("SELECT tl FROM TodoListEntity tl " +
        "LEFT JOIN FETCH tl.todoEntities t " +
        "WHERE tl.userEntity.email = ?1 " +
        "AND tl.uuid = ?2 " +
        "ORDER BY t.position")
    TodoListEntity findByEmailAndListId(String email, String listId);

    @Query("SELECT tl FROM TodoListEntity tl " +
        "WHERE tl.userEntity.email = ?1")
    List<TodoListEntity> findAllOverviews(String email);
}
