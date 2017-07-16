package com.doerapispring.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TodoService {
    private final AggregateRootRepository<MasterList, Todo> todoRepository;
    private final ObjectRepository<CompletedList, String> completedListRepository;
    private final ObjectRepository<MasterList, String> masterListRepository;

    @Autowired
    TodoService(ObjectRepository<MasterList, String> masterListRepository,
                AggregateRootRepository<MasterList, Todo> todoRepository,
                ObjectRepository<CompletedList, String> completedListRepository) {
        this.todoRepository = todoRepository;
        this.completedListRepository = completedListRepository;
        this.masterListRepository = masterListRepository;
    }

    public void create(User user, String task, ScheduledFor scheduling) throws OperationRefusedException {
        MasterList masterList = get(user);
        try {
            // TODO: This should probably just return the localIdentifier, so the Todo has to be retrieved using a get to add it to the repo
            Todo todo = masterList.add(task, scheduling);
            todoRepository.add(masterList, todo);
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
            todoRepository.remove(masterList, todo);
        } catch (TodoNotFoundException | AbnormalModelException e) {
            throw new OperationRefusedException();
        }
    }

    public void displace(User user, String localIdentifier, String task) throws OperationRefusedException {
        try {
            MasterList masterList = get(user);
            List<Todo> newAndExistingTodos = masterList.displace(localIdentifier, task);
            Todo newTodo = newAndExistingTodos.get(0);
            Todo existingTodo = newAndExistingTodos.get(1);
            // TODO: Maybe this should do an add, get, update
            todoRepository.add(masterList, newTodo);
            todoRepository.update(masterList, existingTodo);
        } catch (TodoNotFoundException | DuplicateTodoException | AbnormalModelException e) {
            throw new OperationRefusedException();
        }
    }

    public void update(User user, String localIdentifier, String task) throws OperationRefusedException {
        try {
            MasterList masterList = get(user);
            Todo todo = masterList.update(localIdentifier, task);
            todoRepository.update(masterList, todo);
        } catch (TodoNotFoundException | DuplicateTodoException | AbnormalModelException e) {
            throw new OperationRefusedException();
        }
    }

    public void complete(User user, String localIdentifier) throws OperationRefusedException{
        try {
            MasterList masterList = get(user);
            Todo todo = masterList.complete(localIdentifier);
            todoRepository.update(masterList, todo);
        } catch (TodoNotFoundException | AbnormalModelException e) {
            throw new OperationRefusedException();
        }
    }

    public CompletedList getCompleted(User user) throws OperationRefusedException {
        return completedListRepository.find(user.getIdentifier())
                .orElseThrow(OperationRefusedException::new);
    }

    public void move(User user, String localIdentifier, String targetLocalIdentifier) throws OperationRefusedException {
        try {
            MasterList masterList = get(user);
            List<Todo> todos = masterList.move(localIdentifier, targetLocalIdentifier);
            todoRepository.update(masterList, todos);
        } catch (TodoNotFoundException | AbnormalModelException e) {
            throw new OperationRefusedException();
        }
    }

    public void pull(User user) throws OperationRefusedException {
        try {
            MasterList masterList = get(user);
            List<Todo> todos = masterList.pull();
            todoRepository.update(masterList, todos);
        } catch (ListSizeExceededException | AbnormalModelException e) {
            throw new OperationRefusedException();
        }
    }

    public List<Todo> getDeferredTodos(User user) throws OperationRefusedException {
        try {
            MasterList masterList = get(user);
            return masterList.getDeferredTodos();
        } catch (LockTimerNotExpiredException e) {
            throw new OperationRefusedException();
        }
    }
}
