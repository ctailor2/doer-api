package com.doerapispring.todos;

import com.doerapispring.apiTokens.SessionToken;
import com.doerapispring.apiTokens.SessionTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by chiragtailor on 9/27/16.
 */
@Service
@Transactional
public class TodoService {
    private final TodoRepository todoRepository;
    private final SessionTokenService sessionTokenService;

    @Autowired
    public TodoService(TodoRepository todoRepository, SessionTokenService sessionTokenService) {
        this.todoRepository = todoRepository;
        this.sessionTokenService = sessionTokenService;
    }

    public TodoEntity create(String token, TodoEntity todoEntity) {
        SessionToken sessionToken = sessionTokenService.getByToken(token);
        if (sessionToken != null) {
            Todo todo = Todo.builder()
                    .user(sessionToken.user)
                    .task(todoEntity.getTask())
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();
            todoRepository.save(todo);
            return todoEntity;
        }
        // TODO: Should probably throw an exception here instead
        return null;
    }

    public List<TodoEntity> get(String token) {
        SessionToken sessionToken = sessionTokenService.getByToken(token);
        if (sessionToken != null) {
            List<Todo> todos = todoRepository.findByUserId(sessionToken.user.id);
            List<TodoEntity> todoEntities = todos.stream().map((todo) -> TodoEntity.builder()
                    .task(todo.task)
                    .build()).collect(Collectors.toList());
            return todoEntities;
        }
        // TODO: Should probably throw an exception here instead
        return Collections.emptyList();
    }
}
