package com.doerapispring.messaging;

import com.doerapispring.domain.*;
import com.doerapispring.domain.events.TodoCompletedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.stereotype.Component;

@Component
public class TodoCompletedApplicationEventListener implements ApplicationListener<PayloadApplicationEvent<TodoCompletedEvent>> {
    private final OwnedObjectRepository<CompletedTodoWriteModel, UserId, CompletedTodoId> completedTodoRepository;

    public TodoCompletedApplicationEventListener(
        OwnedObjectRepository<CompletedTodoWriteModel, UserId, CompletedTodoId> completedTodoRepository) {
        this.completedTodoRepository = completedTodoRepository;
    }

    @Override
    public void onApplicationEvent(PayloadApplicationEvent<TodoCompletedEvent> event) {
        TodoCompletedEvent todoCompletedEvent = event.getPayload();
        completedTodoRepository.save(
            new CompletedTodoWriteModel(
                new UserId(todoCompletedEvent.getUserId()),
                new ListId(todoCompletedEvent.getListId()),
                new CompletedTodoId(todoCompletedEvent.getCompletedTodoId()),
                todoCompletedEvent.getTask(),
                todoCompletedEvent.getCompletedAt()));
    }
}
