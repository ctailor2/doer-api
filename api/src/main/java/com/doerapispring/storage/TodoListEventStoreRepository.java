package com.doerapispring.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface TodoListEventStoreRepository extends JpaRepository<TodoListEventStoreEntity, TodoListEventStoreEntityKey> {
    List<TodoListEventStoreEntity> findAllByKeyUserIdAndKeyListIdOrderByKeyVersion(String userId, String listId);
}
