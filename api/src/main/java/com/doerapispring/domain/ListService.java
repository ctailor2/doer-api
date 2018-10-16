package com.doerapispring.domain;

import com.doerapispring.web.InvalidRequestException;
import org.springframework.stereotype.Service;

@Service
public class ListService implements ListApplicationService {
    private final ObjectRepository<TodoList, String> todoListRepository;
    private final ObjectRepository<CompletedList, String> completedListRepository;

    ListService(ObjectRepository<TodoList, String> todoListRepository,
                ObjectRepository<CompletedList, String> completedListRepository) {
        this.todoListRepository = todoListRepository;
        this.completedListRepository = completedListRepository;
    }

    public void unlock(User user) throws InvalidRequestException {
        TodoList todoList = todoListRepository.find(user.getIdentifier())
            .orElseThrow(InvalidRequestException::new);
        try {
            todoList.unlock();
            todoListRepository.save(todoList);
        } catch (LockTimerNotExpiredException | AbnormalModelException e) {
            throw new InvalidRequestException();
        }
    }

    public ReadOnlyTodoList get(User user) throws InvalidRequestException {
        return todoListRepository.find(user.getIdentifier())
            .map(TodoList::read)
            .orElseThrow(InvalidRequestException::new);
    }

    public ReadOnlyCompletedList getCompleted(User user) throws InvalidRequestException {
        return completedListRepository.find(user.getIdentifier())
            .map(CompletedList::read)
            .orElseThrow(InvalidRequestException::new);
    }
}
