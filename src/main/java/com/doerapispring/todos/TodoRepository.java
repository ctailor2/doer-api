package com.doerapispring.todos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by chiragtailor on 9/28/16.
 */
public interface TodoRepository extends JpaRepository<TodoEntity, Long> {
    @Query("SELECT t FROM TodoEntity t INNER JOIN t.userEntity u WHERE u.email = ?1")
    List<TodoEntity> findByUserEmail(String userEmail);

    @Query("SELECT t FROM TodoEntity t INNER JOIN t.userEntity u WHERE u.email = ?1 AND t.active = ?2")
    List<TodoEntity> findByUserEmailAndType(String userEmail, boolean active);
}
