package com.doerapispring.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface TodoDao extends JpaRepository<TodoEntity, Long> {
    @Query("SELECT t FROM TodoEntity t " +
            "INNER JOIN t.userEntity u " +
            "WHERE u.email = ?1 AND t.uuid = ?2")
    TodoEntity findUserTodo(String userEmail, String uuid);
}
