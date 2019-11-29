package com.doerapispring.storage;

import com.doerapispring.domain.*;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Repository
public class CompletedTodoListRepository implements OwnedObjectRepository<CompletedTodoList, UserId, ListId> {
    private final CompletedTodoDAO completedTodoDAO;

    public CompletedTodoListRepository(CompletedTodoDAO completedTodoDAO) {
        this.completedTodoDAO = completedTodoDAO;
    }

    @Override
    public void save(CompletedTodoList model) {
    }

    @Override
    public Optional<CompletedTodoList> find(UserId userId, ListId listId) {
        List<CompletedTodoReadModel> completedTodos = completedTodoDAO.findByUserIdAndListIdOrderByCompletedAtDesc(userId.get(), listId.get()).stream()
            .map(completedTodoEntity ->
                new CompletedTodoReadModel(
                    new CompletedTodoId(completedTodoEntity.uuid),
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
