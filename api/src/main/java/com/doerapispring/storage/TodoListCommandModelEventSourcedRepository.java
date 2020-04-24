package com.doerapispring.storage;

import com.doerapispring.domain.*;
import com.doerapispring.domain.events.DomainEvent;
import com.doerapispring.domain.events.TodoListEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.scala.DefaultScalaModule;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.Clock;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@Repository
public class TodoListCommandModelEventSourcedRepository implements OwnedObjectRepository<TodoListCommandModel, UserId, ListId> {
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final TodoListDao todoListDao;
    private final TodoListEventStoreRepository todoListEventStoreRepository;

    public TodoListCommandModelEventSourcedRepository(Clock clock,
                                                      TodoListDao todoListDao,
                                                      TodoListEventStoreRepository todoListEventStoreRepository) {
        this.clock = clock;
        this.todoListDao = todoListDao;
        this.todoListEventStoreRepository = todoListEventStoreRepository;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new DefaultScalaModule());
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(TodoListCommandModel model) {
        List<TodoListEventStoreEntity> todoListEventStoreEntities = IntStream.range(0, model.getDomainEvents().size())
                .mapToObj(i -> {
                    TodoListEvent todoListEvent = model.getDomainEvents().get(i);
                    return new TodoListEventStoreEntity(
                            new TodoListEventStoreEntityKey(
                                    todoListEvent.userId(),
                                    todoListEvent.listId(),
                                    model.getVersion() + i),
                            todoListEvent.getClass(),
                            serializeEvent(todoListEvent)
                    );
                })
                .collect(toList());
        todoListEventStoreRepository.saveAll(todoListEventStoreEntities);
    }

    private String serializeEvent(DomainEvent domainEvent) {
        try {
            return objectMapper.writeValueAsString(domainEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<TodoListCommandModel> find(UserId userId, ListId listId) {
        TodoListEntity todoListEntity = todoListDao.findByEmailAndListId(userId.get(), listId.get());
        TodoList todoList = new TodoList(
                new UserId(todoListEntity.userEntity.email),
                new ListId(todoListEntity.uuid),
                todoListEntity.name,
                todoListEntity.demarcationIndex,
                Date.from(todoListEntity.lastUnlockedAt.toInstant()));
        List<TodoListEventStoreEntity> eventStoreEntities = todoListEventStoreRepository.findAllByKeyUserIdAndKeyListIdOrderByKeyVersion(userId.get(), listId.get());
        List<DomainEvent> domainEvents =
                eventStoreEntities.stream()
                        .map(this::mapToDomainEvent)
                        .collect(toList());
        return Optional.of(TodoListCommandModel.newInstance(clock, todoList).withEvents(domainEvents));
    }

    private DomainEvent mapToDomainEvent(TodoListEventStoreEntity todoListEventStoreEntity) {
        try {
            return objectMapper.readValue(
                    todoListEventStoreEntity.data,
                    todoListEventStoreEntity.eventClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ListId nextIdentifier() {
        return null;
    }
}
