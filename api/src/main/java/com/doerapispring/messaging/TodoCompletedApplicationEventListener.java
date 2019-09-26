package com.doerapispring.messaging;

import com.doerapispring.domain.*;
import org.springframework.context.ApplicationListener;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.stereotype.Component;

import static java.lang.Thread.currentThread;

@Component
public class TodoCompletedApplicationEventListener implements ApplicationListener<PayloadApplicationEvent<TodoCompleted>> {
    private final OwnedObjectRepository<CompletedTodo, UserId, CompletedTodoId> completedTodoRepository;

    public TodoCompletedApplicationEventListener(OwnedObjectRepository<CompletedTodo, UserId, CompletedTodoId> completedTodoRepository) {
        this.completedTodoRepository = completedTodoRepository;
    }

    @Override
    public void onApplicationEvent(PayloadApplicationEvent<TodoCompleted> event) {
        TodoCompleted todoCompleted = event.getPayload();
        Thread thread = currentThread();
        System.out.println("---handling todo completed event on thread: " + thread.getId() + "---");
        completedTodoRepository.save(
            new CompletedTodo(
                todoCompleted.getUserId(),
                todoCompleted.getListId(),
                todoCompleted.getCompletedTodoId(),
                todoCompleted.getTask(),
                todoCompleted.getCompletedAt()));
    }
}
