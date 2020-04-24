package com.doerapispring.storage;

import com.doerapispring.domain.*;
import com.doerapispring.domain.events.TodoCompletedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.scala.DefaultScalaModule;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Repository
public class CompletedTodoListEventSourcedRepository implements OwnedObjectRepository<CompletedTodoList, UserId, ListId> {
    private final TodoListEventStoreRepository todoListEventStoreRepository;
    private final ObjectMapper objectMapper;

    public CompletedTodoListEventSourcedRepository(TodoListEventStoreRepository todoListEventStoreRepository) {
        this.todoListEventStoreRepository = todoListEventStoreRepository;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new DefaultScalaModule());
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(CompletedTodoList model) {
    }

    @Override
    public Optional<CompletedTodoList> find(UserId userId, ListId listId) {
        List<TodoListEventStoreEntity> eventStoreEntities = todoListEventStoreRepository.findAllByKeyUserIdAndKeyListIdAndEventClassOrderByKeyVersionDesc(userId.get(), listId.get(), TodoCompletedEvent.class);
        List<CompletedTodo> completedTodos = eventStoreEntities.stream()
                .map(this::deserializeEvent)
                .map(todoCompletedEvent ->
                        new CompletedTodo(
                                new CompletedTodoId(todoCompletedEvent.completedTodoId()),
                                todoCompletedEvent.task(),
                                todoCompletedEvent.completedAt()))
                .collect(toList());
        return Optional.of(new CompletedTodoList(userId, listId, completedTodos));
    }

    private TodoCompletedEvent deserializeEvent(TodoListEventStoreEntity eventStoreEntity) {
        try {
            return objectMapper.readValue(eventStoreEntity.data, TodoCompletedEvent.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ListId nextIdentifier() {
        return null;
    }
}
