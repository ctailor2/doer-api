package com.doerapispring.storage;

import com.doerapispring.domain.CompletedTodo;
import com.doerapispring.domain.CompletedTodoId;
import com.doerapispring.domain.OwnedObjectRepository;
import com.doerapispring.domain.UserId;
import org.springframework.stereotype.Repository;
import org.springframework.util.IdGenerator;

@Repository
public class CompletedTodoRepository implements OwnedObjectRepository<CompletedTodo, UserId, CompletedTodoId> {
    private final CompletedTodoDAO completedTodoDAO;
    private final UserDAO userDAO;
    private final IdGenerator idGenerator;

    CompletedTodoRepository(CompletedTodoDAO completedTodoDAO,
                            UserDAO userDAO,
                            IdGenerator idGenerator) {
        this.completedTodoDAO = completedTodoDAO;
        this.userDAO = userDAO;
        this.idGenerator = idGenerator;
    }

    @Override
    public void save(CompletedTodo completedTodo) {
        UserEntity userEntity = userDAO.findByEmail(completedTodo.getUserId().get());
        CompletedTodoEntity completedTodoEntity = CompletedTodoEntity.builder()
            .listId(completedTodo.getListId().get())
            .userId(userEntity.id)
            .task(completedTodo.getTask())
            .completedAt(completedTodo.getCompletedAt())
            .build()
            .withUuid(completedTodo.getCompletedTodoId().get());
        completedTodoDAO.save(completedTodoEntity);
    }

    @Override
    public CompletedTodoId nextIdentifier() {
        return new CompletedTodoId(idGenerator.generateId().toString());
    }
}
