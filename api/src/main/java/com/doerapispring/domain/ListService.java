package com.doerapispring.domain;

import com.doerapispring.web.InvalidRequestException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListService implements ListApplicationService {
    private final OwnedObjectRepository<TodoList, UserId, ListId> todoListRepository;
    private final OwnedObjectRepository<CompletedTodo, UserId, CompletedTodoId> completedTodoRepository;

    ListService(OwnedObjectRepository<TodoList, UserId, ListId> todoListRepository,
                OwnedObjectRepository<CompletedTodo, UserId, CompletedTodoId> completedTodoRepository) {
        this.todoListRepository = todoListRepository;
        this.completedTodoRepository = completedTodoRepository;
    }

    public void unlock(User user, ListId listId) throws InvalidRequestException {
        TodoList todoList = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidRequestException::new);
        try {
            todoList.unlock();
            todoListRepository.save(todoList);
        } catch (LockTimerNotExpiredException e) {
            throw new InvalidRequestException();
        }
    }

    public ReadOnlyTodoList getDefault(User user) throws InvalidRequestException {
        return todoListRepository.findFirst(user.getUserId())
            .map(TodoList::read)
            .orElseThrow(InvalidRequestException::new);
    }

    public List<CompletedTodo> getCompleted(User user, ListId listId) throws InvalidRequestException {
        return completedTodoRepository.findAll(user.getUserId());
    }

    @Override
    public ReadOnlyTodoList get(User user, ListId listId) throws InvalidRequestException {
        return todoListRepository.find(user.getUserId(), listId)
            .map(TodoList::read)
            .orElseThrow(InvalidRequestException::new);
    }
}
