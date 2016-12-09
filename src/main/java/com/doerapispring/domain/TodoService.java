package com.doerapispring.domain;

import com.doerapispring.storage.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TodoService {
    private final TodoRepository todoRepository;

    @Autowired
    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public Todo create(UserIdentifier userIdentifier, String task, ScheduledFor scheduling) throws OperationRefusedException {
        Todo todo = new Todo(userIdentifier, task, scheduling);
        try {
            todoRepository.add(todo);
        } catch (AbnormalModelException e) {
            throw new OperationRefusedException();
        }
        return todo;
    }

    public List<Todo> getByScheduling(UserIdentifier userIdentifier, ScheduledFor scheduling) {
        return todoRepository.findByScheduling(userIdentifier, scheduling);
    }
}
