package com.doerapispring.storage;

import com.doerapispring.domain.CompletedTodoId;
import com.doerapispring.domain.CompletedTodoWriteModel;
import com.doerapispring.domain.OwnedObjectRepository;
import com.doerapispring.domain.UserId;
import org.springframework.stereotype.Repository;

@Repository
public class CompletedTodoRepository implements OwnedObjectRepository<CompletedTodoWriteModel, UserId, CompletedTodoId> {
    private final CompletedTodoDAO completedTodoDAO;

    CompletedTodoRepository(CompletedTodoDAO completedTodoDAO) {
        this.completedTodoDAO = completedTodoDAO;
    }

    @Override
    public void save(CompletedTodoWriteModel completedTodo) {
        CompletedTodoEntity completedTodoEntity = CompletedTodoEntity.builder()
            .uuid(completedTodo.getCompletedTodoId().get())
            .listId(completedTodo.getListId().get())
            .userIdentifier(completedTodo.getUserId().get())
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
