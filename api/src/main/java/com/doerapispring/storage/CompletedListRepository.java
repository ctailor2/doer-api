package com.doerapispring.storage;

import com.doerapispring.domain.*;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Repository
class CompletedListRepository implements ObjectRepository<CompletedList, String> {
    private final Clock clock;
    private final CompletedListDAO completedListDAO;
    private final UserDAO userDAO;

    CompletedListRepository(Clock clock, CompletedListDAO completedListDAO, UserDAO userDAO) {
        this.clock = clock;
        this.completedListDAO = completedListDAO;
        this.userDAO = userDAO;
    }

    @Override
    public Optional<CompletedList> find(UniqueIdentifier<String> uniqueIdentifier) {
        CompletedListEntity completedListEntity = completedListDAO.findByEmail(uniqueIdentifier.get());
        List<CompletedTodo> todos = completedListEntity.getCompletedTodoEntities().stream()
            .map(completedTodoEntity ->
                new CompletedTodo(
                    completedTodoEntity.uuid,
                    completedTodoEntity.task,
                    completedTodoEntity.completedAt))
            .collect(toList());
        CompletedList completedList = new CompletedList(clock, uniqueIdentifier, todos);
        return Optional.of(completedList);
    }

    @Override
    public void save(CompletedList completedList) throws AbnormalModelException {
        String email = completedList.getIdentifier().get();
        UserEntity userEntity = userDAO.findByEmail(email);

        List<CompletedTodoEntity> completedTodoEntities = completedList.getTodos().stream()
            .map(completedTodo -> CompletedTodoEntity.builder()
                .uuid(completedTodo.getLocalIdentifier())
                .task(completedTodo.getTask())
                .completedAt(completedTodo.getCompletedAt())
                .build())
            .collect(toList());

        CompletedListEntity completedListEntity = new CompletedListEntity();
        completedListEntity.id = userEntity.id;
        completedListEntity.email = email;
        completedListEntity.getCompletedTodoEntities().addAll(completedTodoEntities);
        completedListDAO.save(completedListEntity);
    }
}
