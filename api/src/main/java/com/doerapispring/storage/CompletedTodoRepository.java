package com.doerapispring.storage;

import com.doerapispring.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.IdGenerator;

import java.sql.Date;
import java.util.List;

import static java.util.stream.Collectors.toList;

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
    public void save(CompletedTodo completedTodo) throws AbnormalModelException {
        UserEntity userEntity = userDAO.findByEmail(completedTodo.getUserId().get());
        completedTodoDAO.save(CompletedTodoEntity.builder()
            .listId(completedTodo.getListId().get())
            .userId(userEntity.id)
            .uuid(completedTodo.getCompletedTodoId().get())
            .task(completedTodo.getTask())
            .completedAt(completedTodo.getCompletedAt())
            .build());
    }

    @Override
    public List<CompletedTodo> findAll(UserId userId) {
        UserEntity userEntity = userDAO.findByEmail(userId.get());
        return completedTodoDAO.findByUserIdOrderByCompletedAtDesc(userEntity.id).stream()
            .map(completedTodoEntity ->
                new CompletedTodo(
                    new UserId(userEntity.email),
                    new ListId(completedTodoEntity.listId),
                    new CompletedTodoId(completedTodoEntity.uuid),
                    completedTodoEntity.task,
                    Date.from(completedTodoEntity.completedAt.toInstant())))
            .collect(toList());
    }

    @Override
    public CompletedTodoId nextIdentifier() {
        return new CompletedTodoId(idGenerator.generateId().toString());
    }
}
