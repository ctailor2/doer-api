package com.doerapispring.api;

import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.OperationRefusedException;
import com.doerapispring.domain.ScheduledFor;
import com.doerapispring.domain.Todo;
import com.doerapispring.domain.TodoService;
import com.doerapispring.web.InvalidRequestException;
import com.doerapispring.web.TodoApiService;
import com.doerapispring.web.TodoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TodoApiServiceImpl implements TodoApiService {
    private final TodoService todoService;

    @Autowired
    public TodoApiServiceImpl(TodoService todoService) {
        this.todoService = todoService;
    }

    @Override
    public List<TodoDTO> getByScheduling(AuthenticatedUser authenticatedUser, String scheduling) throws InvalidRequestException {
        ScheduledFor scheduledFor = getScheduledFor(scheduling);
        List<Todo> todos = todoService.getByScheduling(authenticatedUser.getUser(), scheduledFor);
        return todos.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public void create(AuthenticatedUser authenticatedUser, String task, String scheduling) throws InvalidRequestException {
        ScheduledFor scheduledFor = getScheduledFor(scheduling);
        try {
            todoService.create(authenticatedUser.getUser(), task, scheduledFor);
        } catch (OperationRefusedException e) {
            throw new InvalidRequestException();
        }
    }

    // TODO: Maybe these behaviors should exist in a mapping layer of some sort

    private TodoDTO mapToDTO(Todo todo) {
        return new TodoDTO(todo.getLocalIdentifier(), todo.getTask(), todo.getScheduling().toString());
    }

    private ScheduledFor getScheduledFor(String scheduling) throws InvalidRequestException {
        try {
            return Enum.valueOf(ScheduledFor.class, scheduling);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException();
        }
    }
}
