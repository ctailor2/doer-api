package com.doerapispring.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TodoService {
    private final AggregateRootRepository<MasterList, Todo, String> masterListRepository;
    private final AggregateRootRepository<CompletedList, CompletedTodo, String> completedListRepository;

    @Autowired
    TodoService(AggregateRootRepository<MasterList, Todo, String> masterListRepository,
                AggregateRootRepository<CompletedList, CompletedTodo, String> completedListRepository) {
        this.masterListRepository = masterListRepository;
        this.completedListRepository = completedListRepository;
    }

    public void create(User user, String task, ScheduledFor scheduling) throws OperationRefusedException {
        MasterList masterList = get(user);
        try {
            // TODO: This should probably just return the localIdentifier, so the Todo has to be retrieved using a get to add it to the repo
            Todo todo = masterList.add(task, scheduling);
            masterListRepository.add(masterList, todo);
        } catch (ListSizeExceededException | DuplicateTodoException | AbnormalModelException e) {
            throw new OperationRefusedException();
        }
    }

    public MasterList get(User user) throws OperationRefusedException {
        return masterListRepository.find(user.getIdentifier())
                .orElseThrow(OperationRefusedException::new);
    }

    public void delete(User user, String localIdentifier) throws OperationRefusedException {
        try {
            MasterList masterList = get(user);
            Todo todo = masterList.delete(localIdentifier);
            masterListRepository.remove(masterList, todo);
        } catch (TodoNotFoundException | AbnormalModelException e) {
            throw new OperationRefusedException();
        }
    }

    public void displace(User user, String localIdentifier, String task) throws OperationRefusedException {
        try {
            MasterList masterList = get(user);
            List<Todo> newAndExistingTodos = masterList.displace(localIdentifier, task);
            // TODO: This stinks, fix it
            Todo newTodo = newAndExistingTodos.get(0);
            Todo existingTodo = newAndExistingTodos.get(1);
            masterListRepository.add(masterList, newTodo);
            masterListRepository.update(masterList, existingTodo);
        } catch (TodoNotFoundException | DuplicateTodoException | AbnormalModelException e) {
            throw new OperationRefusedException();
        }
    }

    public void update(User user, String localIdentifier, String task) throws OperationRefusedException {
        try {
            MasterList masterList = get(user);
            Todo todo = masterList.update(localIdentifier, task);
            masterListRepository.update(masterList, todo);
        } catch (TodoNotFoundException | DuplicateTodoException | AbnormalModelException e) {
            throw new OperationRefusedException();
        }
    }

    public void complete(User user, String localIdentifier) throws OperationRefusedException{
        try {
            MasterList masterList = get(user);
            Todo todo = masterList.complete(localIdentifier);
            masterListRepository.update(masterList, todo);
        } catch (TodoNotFoundException | AbnormalModelException e) {
            throw new OperationRefusedException();
        }
    }

    public CompletedList getCompleted(User user) throws OperationRefusedException {
        return completedListRepository.find(user.getIdentifier())
                .orElseThrow(OperationRefusedException::new);
    }
}
