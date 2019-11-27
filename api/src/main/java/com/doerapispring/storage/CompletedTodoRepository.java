package com.doerapispring.storage;

import com.doerapispring.domain.CompletedTodo;
import com.doerapispring.domain.CompletedTodoId;
import com.doerapispring.domain.OwnedObjectRepository;
import com.doerapispring.domain.UserId;
import org.springframework.stereotype.Repository;

@Repository
public class CompletedTodoRepository implements OwnedObjectRepository<CompletedTodo, UserId, CompletedTodoId> {
    private final CompletedTodoDAO completedTodoDAO;
    private final UserDAO userDAO;

    CompletedTodoRepository(CompletedTodoDAO completedTodoDAO,
                            UserDAO userDAO) {
        this.completedTodoDAO = completedTodoDAO;
        this.userDAO = userDAO;
    }

    @Override
    public void save(CompletedTodo completedTodo) {
        UserEntity userEntity = userDAO.findByEmail(completedTodo.getUserId().get());
        CompletedTodoEntity completedTodoEntity = CompletedTodoEntity.builder()
            .uuid(completedTodo.getCompletedTodoId().get())
            .listId(completedTodo.getListId().get())
            .userId(userEntity.id)
            .task(completedTodo.getTask())
            .completedAt(completedTodo.getCompletedAt())
            .build();
        completedTodoDAO.save(completedTodoEntity);
    }

    @Override
    public CompletedTodoId nextIdentifier() {
        return null;
    }
}
