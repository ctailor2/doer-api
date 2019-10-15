package com.doerapispring.domain;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListService implements ListApplicationService {
    private final OwnedObjectRepository<TodoListCommandModel, UserId, ListId> todoListRepository;
    private final OwnedObjectRepository<CompletedTodo, UserId, CompletedTodoId> completedTodoRepository;
    private final OwnedObjectRepository<ListOverview, UserId, ListId> listOverviewRepository;
    private final TodoListFactory todoListFactory;

    ListService(OwnedObjectRepository<TodoListCommandModel, UserId, ListId> todoListRepository,
                OwnedObjectRepository<CompletedTodo, UserId, CompletedTodoId> completedTodoRepository,
                OwnedObjectRepository<ListOverview, UserId, ListId> listOverviewRepository,
                TodoListFactory todoListFactory) {
        this.todoListRepository = todoListRepository;
        this.completedTodoRepository = completedTodoRepository;
        this.listOverviewRepository = listOverviewRepository;
        this.todoListFactory = todoListFactory;
    }

    public void unlock(User user, ListId listId) throws InvalidCommandException {
        TodoListCommandModel todoListCommandModel = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidCommandException::new);
        try {
            todoListCommandModel.unlock();
            todoListRepository.save(todoListCommandModel);
        } catch (LockTimerNotExpiredException e) {
            throw new InvalidCommandException();
        }
    }

    public TodoListReadModel getDefault(User user) throws InvalidCommandException {
        return todoListRepository.findFirst(user.getUserId())
            .map(TodoListCommandModel::read)
            .orElseThrow(InvalidCommandException::new);
    }

    public List<CompletedTodo> getCompleted(User user, ListId listId) throws InvalidCommandException {
        return completedTodoRepository.findAll(user.getUserId());
    }

    @Override
    public TodoListReadModel get(User user, ListId listId) throws InvalidCommandException {
        return todoListRepository.find(user.getUserId(), listId)
            .map(TodoListCommandModel::read)
            .orElseThrow(InvalidCommandException::new);
    }

    @Override
    public List<ListOverview> getOverviews(User user) {
        return listOverviewRepository.findAll(user.getUserId());
    }

    @Override
    public void create(User user, String name) {
        ListId listId = listOverviewRepository.nextIdentifier();
        ListOverview listOverview = todoListFactory.listOverview(user.getUserId(), listId, name);
        listOverviewRepository.save(listOverview);
    }
}
