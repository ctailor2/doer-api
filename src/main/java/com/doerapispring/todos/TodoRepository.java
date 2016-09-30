package com.doerapispring.todos;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by chiragtailor on 9/28/16.
 */
public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByUserId(long userId);
}
