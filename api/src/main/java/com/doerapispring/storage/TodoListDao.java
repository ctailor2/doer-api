package com.doerapispring.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

interface TodoListDao extends JpaRepository<TodoListEntity, Long> {
    @Query("SELECT tl FROM TodoListEntity tl " +
        "WHERE tl.userEntity.email = ?1 " +
        "AND tl.uuid = ?2")
    TodoListEntity findByEmailAndListId(String email, String listId);

    @Query("SELECT tl FROM TodoListEntity tl " +
        "WHERE tl.userEntity.email = ?1")
    List<TodoListEntity> findByEmailWithoutTodos(String email);
}
