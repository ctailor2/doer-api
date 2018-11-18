package com.doerapispring.storage;

import com.doerapispring.domain.*;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Repository
public class CompletedTodoRepository implements OwnedObjectRepository<CompletedTodo, UserId, CompletedTodoId> {
    private final CompletedTodoDAO completedTodoDAO;
    private final UserDAO userDAO;

    CompletedTodoRepository(CompletedTodoDAO completedTodoDAO, UserDAO userDAO) {
        this.completedTodoDAO = completedTodoDAO;
        this.userDAO = userDAO;
    }

    @Override
    public void save(CompletedTodo completedTodo) throws AbnormalModelException {
        UserEntity userEntity = userDAO.findByEmail(completedTodo.getUserId().get());
        completedTodoDAO.save(CompletedTodoEntity.builder()
            .userId(userEntity.id)
            .uuid(completedTodo.getCompletedTodoId().getIdentifier())
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
                    new CompletedTodoId(completedTodoEntity.uuid),
                    completedTodoEntity.task,
                    Date.from(completedTodoEntity.completedAt.toInstant())
                ))
            .collect(toList());
    }
}
