package com.doerapispring.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TodoService {
    private final AggregateRootRepository<MasterList, Todo, String> masterListRepository;

    @Autowired
    public TodoService(AggregateRootRepository<MasterList, Todo, String> masterListRepository) {
        this.masterListRepository = masterListRepository;
    }

    public Todo create(User user, String task, ScheduledFor scheduling) throws OperationRefusedException {
        // TODO: Maybe the todo creation method should live on the list like object?
        // For example, throw in the task and how you want it scheduled - out comes a domain object
        // This domain object has some local identifier, unique only within the aggregate
        Todo todo = new Todo(task, scheduling);
        Optional<MasterList> masterList = masterListRepository.find(user.getIdentifier());
        if (!masterList.isPresent()) throw new OperationRefusedException();
        try {
            masterListRepository.add(masterList.get(), todo);
        } catch (AbnormalModelException e) {
            throw new OperationRefusedException();
        }
        return todo;
    }

    public List<Todo> getByScheduling(User user, ScheduledFor scheduling) {
        Optional<MasterList> masterList = masterListRepository.find(user.getIdentifier());
        if (!masterList.isPresent()) return Collections.emptyList();
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
