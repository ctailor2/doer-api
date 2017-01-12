package com.doerapispring.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

interface TodoDao extends JpaRepository<TodoEntity, Long> {
    @Query("SELECT t FROM TodoEntity t INNER JOIN t.userEntity u WHERE t.completed = false AND u.email = ?1")
    List<TodoEntity> findUnfinishedByUserEmail(String userEmail);
}
