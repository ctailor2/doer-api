package com.doerapispring.domain;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListService implements ListApplicationService {
    private final OwnedObjectRepository<TodoListCommandModel, UserId, ListId> todoListCommandModelRepository;
    private final OwnedObjectRepository<CompletedTodoList, UserId, ListId> completedTodoRepository;
    private final OwnedObjectRepository<TodoList, UserId, ListId> todoListRepository;
    private final TodoListFactory todoListFactory;
    private final ObjectRepository<User, UserId> userRepository;

    ListService(OwnedObjectRepository<TodoListCommandModel, UserId, ListId> todoListCommandModelRepository,
                OwnedObjectRepository<CompletedTodoList, UserId, ListId> completedTodoRepository,
                OwnedObjectRepository<TodoList, UserId, ListId> todoListRepository,
                TodoListFactory todoListFactory,
                ObjectRepository<User, UserId> userRepository) {
        this.todoListCommandModelRepository = todoListCommandModelRepository;
        this.completedTodoRepository = completedTodoRepository;
        this.todoListRepository = todoListRepository;
        this.todoListFactory = todoListFactory;
        this.userRepository = userRepository;
    }

    public void unlock(User user, ListId listId) {
        TodoListCommandModel todoListCommandModel = todoListCommandModelRepository.find(user.getUserId(), listId)
            .orElseThrow(ListNotFoundException::new);
        todoListCommandModel.unlock();
        todoListCommandModelRepository.save(todoListCommandModel);
    }

    public TodoListReadModel getDefault(User user) {
        return todoListCommandModelRepository.find(user.getUserId(), user.getDefaultListId())
            .map(TodoListCommandModel::read)
            .orElseThrow(ListNotFoundException::new);
    }

    @Override
    public CompletedTodoList getCompleted(User user, ListId listId) {
        return completedTodoRepository.find(user.getUserId(), listId)
            .orElseThrow(ListNotFoundException::new);
    }

    @Override
    public TodoListReadModel get(User user, ListId listId) {
        return todoListCommandModelRepository.find(user.getUserId(), listId)
            .map(TodoListCommandModel::read)
            .orElseThrow(ListNotFoundException::new);
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

    @Override
    public void setDefault(User user, ListId listId) {
        userRepository.save(new User(user.getUserId(), listId));
    }
}
