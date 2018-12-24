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
}
