package com.doerapispring.storage;

import com.doerapispring.domain.CompletedTodoList;
import com.doerapispring.domain.ListId;
import com.doerapispring.domain.OwnedObjectRepository;
import com.doerapispring.domain.UserId;
import com.doerapispring.domain.events.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@Repository
public class CompletedTodoListEventSourcedRepository implements OwnedObjectRepository<CompletedTodoList, UserId, ListId> {
    private final TodoListEventStoreRepository todoListEventStoreRepository;
    private final ObjectMapper objectMapper;

    public CompletedTodoListEventSourcedRepository(TodoListEventStoreRepository todoListEventStoreRepository,
                                                   ObjectMapper objectMapper) {
        this.todoListEventStoreRepository = todoListEventStoreRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(CompletedTodoList model) {
    }

    @Override
    public Optional<CompletedTodoList> find(UserId userId, ListId listId) {
        List<TodoListEventStoreEntity> eventStoreEntities = todoListEventStoreRepository.findAllByKeyUserIdAndKeyListIdAndEventClassInOrderByKeyVersion(
                userId.get(),
                listId.get(),
                asList(TodoAddedEvent.class,
                        DeferredTodoAddedEvent.class,
                        TodoCompletedEvent.class,
                        TodoDisplacedEvent.class));
        List<TimestampedDomainEvent> timestampedDomainEvents = eventStoreEntities.stream()
                .map(this::deserializeEvent)
                .collect(Collectors.toList());
        return Optional.of(new CompletedTodoList(userId, listId).withEvents(timestampedDomainEvents));
    }

    private TimestampedDomainEvent deserializeEvent(TodoListEventStoreEntity eventStoreEntity) {
        try {
            DomainEvent domainEvent = objectMapper.readValue(eventStoreEntity.data, eventStoreEntity.eventClass);
            return new TimestampedDomainEvent(domainEvent, eventStoreEntity.createdAt);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ListId nextIdentifier() {
        return null;
    }
}
