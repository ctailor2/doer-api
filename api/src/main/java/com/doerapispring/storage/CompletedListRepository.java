package com.doerapispring.storage;

import com.doerapispring.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.IdGenerator;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Repository
class CompletedListRepository implements
    IdentityGeneratingObjectRepository<CompletedList, String> {
    private final Clock clock;
    private final CompletedListDAO completedListDAO;
    private final UserDAO userDAO;
    private final IdGenerator idGenerator;

    CompletedListRepository(Clock clock, CompletedListDAO completedListDAO, UserDAO userDAO, IdGenerator idGenerator) {
        this.clock = clock;
        this.completedListDAO = completedListDAO;
        this.userDAO = userDAO;
        this.idGenerator = idGenerator;
    }

    @Override
    public Optional<CompletedList> find(UniqueIdentifier<String> uniqueIdentifier) {
        CompletedListEntity completedListEntity = completedListDAO.findByEmail(uniqueIdentifier.get());
        List<CompletedTodo> todos = completedListEntity.getCompletedTodoEntities().stream()
            .map(completedTodoEntity ->
                new CompletedTodo(
                    new CompletedTodoId(completedTodoEntity.uuid),
                    completedTodoEntity.task,
                    completedTodoEntity.completedAt))
            .collect(toList());
        CompletedList completedList = new CompletedList(clock, uniqueIdentifier, todos);
        return Optional.of(completedList);
    }

    @Override
    public UniqueIdentifier<String> nextIdentifier() {
        return new UniqueIdentifier<>(idGenerator.generateId().toString());
    }

    @Override
    public void save(CompletedList completedList) throws AbnormalModelException {
        String email = completedList.getIdentifier().get();
        UserEntity userEntity = userDAO.findByEmail(email);

        List<CompletedTodoEntity> completedTodoEntities = completedList.getTodos().stream()
            .map(completedTodo -> CompletedTodoEntity.builder()
                .uuid(completedTodo.getCompletedTodoId().getIdentifier())
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
