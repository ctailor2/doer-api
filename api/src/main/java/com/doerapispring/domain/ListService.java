package com.doerapispring.domain;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListService implements ListApplicationService {
    private final OwnedObjectRepository<TodoListCommandModel, UserId, ListId> todoListCommandModelRepository;
    private final OwnedObjectRepository<CompletedTodoList, UserId, ListId> completedTodoRepository;
    private final OwnedObjectRepository<TodoList, UserId, ListId> todoListRepository;
    private final TodoListFactory todoListFactory;

    ListService(OwnedObjectRepository<TodoListCommandModel, UserId, ListId> todoListCommandModelRepository,
                OwnedObjectRepository<CompletedTodoList, UserId, ListId> completedTodoRepository,
                OwnedObjectRepository<TodoList, UserId, ListId> todoListRepository,
                TodoListFactory todoListFactory) {
        this.todoListCommandModelRepository = todoListCommandModelRepository;
        this.completedTodoRepository = completedTodoRepository;
        this.todoListRepository = todoListRepository;
        this.todoListFactory = todoListFactory;
    }

    public void unlock(User user, ListId listId) throws InvalidCommandException {
        TodoListCommandModel todoListCommandModel = todoListCommandModelRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidCommandException::new);
        try {
            todoListCommandModel.unlock();
            todoListCommandModelRepository.save(todoListCommandModel);
        } catch (LockTimerNotExpiredException e) {
            throw new InvalidCommandException();
        }
    }

    public TodoListReadModel getDefault(User user) throws InvalidCommandException {
        return todoListCommandModelRepository.find(user.getUserId(), user.getDefaultListId())
            .map(TodoListCommandModel::read)
            .orElseThrow(InvalidCommandException::new);
    }

    @Override
    public CompletedTodoList getCompleted(User user, ListId listId) throws InvalidCommandException {
        return completedTodoRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidCommandException::new);
    }

    @Override
    public TodoListReadModel get(User user, ListId listId) throws InvalidCommandException {
        return todoListCommandModelRepository.find(user.getUserId(), listId)
            .map(TodoListCommandModel::read)
            .orElseThrow(InvalidCommandException::new);
    }

    @Override
    public List<TodoList> getAll(User user) {
        return todoListRepository.findAll(user.getUserId());
    }

    @Override
    public void create(User user, String name) {
        ListId listId = todoListRepository.nextIdentifier();
        TodoList todoList = todoListFactory.todoList(user.getUserId(), listId, name);
        todoListRepository.save(todoList);
    }
}
