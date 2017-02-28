package com.doerapispring.storage;

import com.doerapispring.domain.AggregateRootRepository;
import com.doerapispring.domain.CompletedList;
import com.doerapispring.domain.CompletedTodo;
import com.doerapispring.domain.UniqueIdentifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
class CompletedListRepository implements AggregateRootRepository<CompletedList, CompletedTodo, String> {
    private final TodoDao todoDAO;

    CompletedListRepository(TodoDao todoDAO) {
        this.todoDAO = todoDAO;
    }

    @Override
    public Optional<CompletedList> find(UniqueIdentifier<String> uniqueIdentifier) {
        String email = uniqueIdentifier.get();
        List<TodoEntity> todoEntities = todoDAO.findFinishedByUserEmail(email);
        List<CompletedTodo> todos = todoEntities.stream()
                .map(todoEntity -> new CompletedTodo(
                        todoEntity.task,
                        todoEntity.updatedAt))
                .collect(Collectors.toList());
        CompletedList completedList = new CompletedList(uniqueIdentifier, todos);
        return Optional.of(completedList);
    }
}
