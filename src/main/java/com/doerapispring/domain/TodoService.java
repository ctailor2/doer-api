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

    public void create(User user, String task, ScheduledFor scheduling) throws OperationRefusedException {
        // TODO: Maybe the todo creation method should live on the list like object?
        // For example, throw in the task and how you want it scheduled - out comes a domain object
        // This domain object has some local identifier, unique only within the aggregate
        Optional<MasterList> masterListOptional = masterListRepository.find(user.getIdentifier());
        if (!masterListOptional.isPresent()) throw new OperationRefusedException();
        MasterList masterList = masterListOptional.get();
        Todo todo = null;
        try {
            todo = masterList.add(task, scheduling);
        } catch (ListSizeExceededException e) {
            // TODO: Drive this behavior with tests
            e.printStackTrace();
        }
        try {
            masterListRepository.add(masterList, todo);
        } catch (AbnormalModelException e) {
            throw new OperationRefusedException();
        }
    }

    public List<Todo> getByScheduling(User user, ScheduledFor scheduling) {
        Optional<MasterList> masterListOptional = masterListRepository.find(user.getIdentifier());
        if (!masterListOptional.isPresent()) return Collections.emptyList();
        MasterList masterList = masterListOptional.get();
        switch (scheduling) {
            case now:
                return masterList.getImmediateList().getTodos();
            case later:
                return masterList.getPostponedList().getTodos();
            default:
                return masterList.getAllTodos();
        }
    }

    public void displace(User user, String todoLocalIdentifier, String task) {
        Optional<MasterList> masterListOptional = masterListRepository.find(user.getIdentifier());
        masterListOptional.get().displace(todoLocalIdentifier, task);
    }

    public MasterList get(User user) {
        return masterListRepository.find(user.getIdentifier()).get();
    }
}
