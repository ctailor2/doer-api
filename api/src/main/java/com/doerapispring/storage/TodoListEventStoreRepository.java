package com.doerapispring.storage;

import com.doerapispring.domain.events.DomainEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface TodoListEventStoreRepository extends JpaRepository<TodoListEventStoreEntity, TodoListEventStoreEntityKey> {
    List<TodoListEventStoreEntity> findAllByKeyUserIdAndKeyListIdOrderByKeyVersion(String userId, String listId);

    List<TodoListEventStoreEntity> findAllByKeyUserIdAndKeyListIdAndEventClassOrderByKeyVersionDesc(String userId, String listId, Class<? extends DomainEvent> eventClass);
}
