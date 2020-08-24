package com.doerapispring.storage;

import com.doerapispring.domain.events.DomainEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public interface TodoListEventStoreRepository extends JpaRepository<TodoListEventStoreEntity, TodoListEventStoreEntityKey> {
    Optional<TodoListEventStoreEntity> findTopByKeyUserIdAndKeyListIdOrderByKeyVersionDesc(String userId, String listId);

    List<TodoListEventStoreEntity> findAllByKeyUserIdAndKeyListIdAndCreatedAtAfterOrderByKeyVersion(String userId, String listId, Date createdAt);

    List<TodoListEventStoreEntity> findAllByKeyUserIdAndKeyListIdAndEventClassInOrderByKeyVersion(String userId, String listId, List<Class<? extends DomainEvent>> eventClass);
}
