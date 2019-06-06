package com.doerapispring.storage;

import com.doerapispring.domain.*;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Repository
public class CompletedTodoListRepository implements OwnedObjectRepository<CompletedTodoList, UserId, ListId> {
    private final CompletedTodoDAO completedTodoDAO;

    CompletedTodoListRepository(CompletedTodoDAO completedTodoDAO) {
        this.completedTodoDAO = completedTodoDAO;
    }

    @Override
    public Optional<CompletedTodoList> find(UserId userId, ListId listId) {
        List<CompletedTodoEntity> completedTodoEntities =
            completedTodoDAO.findByUserIdOrderByCompletedAtDesc(
                userId.get(),
                listId.get());
        List<CompletedTodo> completedTodos = completedTodoEntities.stream()
            .map(completedTodoEntity ->
                new CompletedTodo(
                    userId,
                    listId,
                    new CompletedTodoId(completedTodoEntity.id),
                    completedTodoEntity.task,
                    Date.from(completedTodoEntity.completedAt.toInstant())))
            .collect(toList());
        return Optional.of(new CompletedTodoList(userId, listId, completedTodos));
    }

    @Override
    public ListId nextIdentifier() {
        return null;
    }
}
