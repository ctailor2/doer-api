package com.doerapispring.todos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by chiragtailor on 9/28/16.
 */
public interface TodoRepository extends JpaRepository<Todo, Long> {
    @Query("SELECT t FROM Todo t INNER JOIN t.user u WHERE u.email = ?1")
    List<Todo> findByUserEmail(String userEmail);
}
