package com.doerapispring.api;

import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.ListService;
import com.doerapispring.domain.OperationRefusedException;
import com.doerapispring.domain.TodoList;
import com.doerapispring.web.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
class ListApiServiceImpl implements ListApiService {
    private final ListService listService;

    @Autowired
    ListApiServiceImpl(ListService listService) {
        this.listService = listService;
    }

    @Override
    public void unlock(AuthenticatedUser authenticatedUser) throws InvalidRequestException {
        try {
            listService.unlock(authenticatedUser.getUser());
        } catch (OperationRefusedException e) {
            throw new InvalidRequestException();
        }
    }

    @Override
    public List<ListDTO> getAll(AuthenticatedUser authenticatedUser) {
        return listService.getAll().stream()
                .map(basicTodoList -> new ListDTO(basicTodoList.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public TodoListDTO get(AuthenticatedUser authenticatedUser) throws InvalidRequestException {
        try {
            TodoList todoList = listService.get(authenticatedUser.getUser());
            List<TodoDTO> todoDTOs = todoList.getTodos()
                    .stream()
                    .map(todo -> new TodoDTO(todo.getLocalIdentifier(), todo.getTask(), todo.getScheduling().toString()))
                    .collect(Collectors.toList());
            return new TodoListDTO(todoList.getScheduling().toString(), todoDTOs, todoList.isFull());
        } catch (OperationRefusedException e) {
            throw new InvalidRequestException();
        }
    }
}
