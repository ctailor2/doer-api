package com.doerapispring.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TodoService {
    private final DomainRepository<Todo, String> todoRepository;
    private final DomainRepository<MasterList, String> masterListRepository;

    @Autowired
    public TodoService(DomainRepository<Todo, String> todoRepository, DomainRepository<MasterList, String> masterListRepository) {
        this.todoRepository = todoRepository;
        this.masterListRepository = masterListRepository;
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
        // Also unique identifiers probably don't need to be their own classes.
        // They just need to have distinct identifier types (String, int, etc.)
        // Maybe the domain repository should take a 2nd type in its definition for the UniqueIdentifier type??
        Optional<MasterList> masterList = masterListRepository.find(userIdentifier);
        switch (scheduling) {
            case now:
                return masterList.get().getImmediateList().getTodos();
            case later:
                return masterList.get().getPostponedList().getTodos();
            default:
                return masterList.get().getAllTodos();
        }
    }
}
