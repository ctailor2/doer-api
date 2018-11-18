package com.doerapispring.storage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface CompletedTodoDAO extends JpaRepository<CompletedTodoEntity, String> {
    List<CompletedTodoEntity> findByUserIdOrderByCompletedAtDesc(Long userId);
}
