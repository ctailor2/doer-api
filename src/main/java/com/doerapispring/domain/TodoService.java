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

    public Todo create(UserIdentifier userIdentifier, String task, ScheduledFor scheduling) throws OperationRefusedException {
        Todo todo = new Todo(task, scheduling);
        Optional<MasterList> masterList = masterListRepository.find(userIdentifier);
        if (!masterList.isPresent()) throw new OperationRefusedException();
        try {
            masterListRepository.add(masterList.get(), todo);
        } catch (AbnormalModelException e) {
            throw new OperationRefusedException();
        }
        return todo;
    }

    public List<Todo> getByScheduling(UserIdentifier userIdentifier, ScheduledFor scheduling) {
        // Also unique identifiers probably don't need to be their own classes.
        // They just need to have distinct identifier types (String, int, etc.)
        // Maybe the object repository should take a 2nd type in its definition for the UniqueIdentifier type??
        // TODO: ^^ implement this
        Optional<MasterList> masterList = masterListRepository.find(userIdentifier);
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
