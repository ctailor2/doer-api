package com.doerapispring.todos;

import com.doerapispring.users.User;
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

    public TodoEntity create(String userEmail, TodoEntity todoEntity) {
        User user = userRepository.findByEmail(userEmail);
        if(user == null) return null;
        Todo todo = Todo.builder()
                .user(user)
                .task(todoEntity.getTask())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        todoRepository.save(todo);
        return todoEntity;
    }

    public List<TodoEntity> get(String userEmail) {
        List<Todo> todos = todoRepository.findByUserEmail(userEmail);
        List<TodoEntity> todoEntities = todos.stream().map((todo) -> TodoEntity.builder()
                .task(todo.task)
                .build()).collect(Collectors.toList());
        return todoEntities;
    }
}
