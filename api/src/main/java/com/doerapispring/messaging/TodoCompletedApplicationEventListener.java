package com.doerapispring.messaging;

import com.doerapispring.domain.*;
import org.springframework.context.ApplicationListener;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.stereotype.Component;

@Component
public class TodoCompletedApplicationEventListener implements ApplicationListener<PayloadApplicationEvent<TodoCompletedEvent>> {
    private final OwnedObjectRepository<CompletedTodo, UserId, CompletedTodoId> completedTodoRepository;

    public TodoCompletedApplicationEventListener(
        OwnedObjectRepository<CompletedTodo, UserId, CompletedTodoId> completedTodoRepository) {
        this.completedTodoRepository = completedTodoRepository;
    }

    @Override
    public void onApplicationEvent(PayloadApplicationEvent<TodoCompletedEvent> event) {
        TodoCompletedEvent todoCompletedEvent = event.getPayload();
        completedTodoRepository.save(
            new CompletedTodo(
                todoCompletedEvent.getUserId(),
                todoCompletedEvent.getListId(),
                todoCompletedEvent.getCompletedTodoId(),
                todoCompletedEvent.getTask(),
                todoCompletedEvent.getCompletedAt()));
    }
}
