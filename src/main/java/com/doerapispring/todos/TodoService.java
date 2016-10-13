package com.doerapispring.todos;

import com.doerapispring.users.UserEntity;
import com.doerapispring.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final UserRepository userRepository;

    @Autowired
    public TodoService(TodoRepository todoRepository, UserRepository userRepository) {
        this.todoRepository = todoRepository;
        this.userRepository = userRepository;
    }

    public Todo create(String userEmail, Todo todo) {
        UserEntity userEntity = userRepository.findByEmail(userEmail);
        if (userEntity == null) return null;
        TodoEntity todoEntity = TodoEntity.builder()
                .userEntity(userEntity)
                .task(todo.getTask())
                .active(todo.isActive())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        todoRepository.save(todoEntity);
        return todo;
    }

    public List<Todo> get(String userEmail) {
        List<TodoEntity> todoEntities = todoRepository.findByUserEmail(userEmail);
        List<Todo> todos = todoEntities.stream().map(todoEntity -> Todo.builder()
                .task(todoEntity.task)
                .active(todoEntity.active)
                .build()).collect(Collectors.toList());
        return todos;
    }
}
