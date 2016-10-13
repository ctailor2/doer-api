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

    // TODO: This null check stinks - try to eliminate it. Maybe overload this method?
    // TODO: The 2 enums are so similar, but they are kept separate to avoid mixing concerns. Still something doesn't seem right.

    // NOTE: Made type an enum instead of a bool bc in the case that no param is sent
    // and the consumer expects all todos to be returned, there is no good default case
    // to use

    // NOTE: Maybe this filters (and any future ones) should be under a ?filters query param
    // this may help to handle the behaviors differently and isolate filtering logic
    public List<Todo> get(String userEmail, TodoTypeParamEnum type) {
        List<TodoEntity> todoEntities;
        if (type == null) {
            todoEntities = todoRepository.findByUserEmail(userEmail);
        } else {
            TodoTypeQueryEnum todoTypeQueryEnum = Enum.valueOf(TodoTypeQueryEnum.class, type.toString());
            todoEntities = todoRepository.findByUserEmailAndType(userEmail, todoTypeQueryEnum.getValue());
        }
        List<Todo> todos = todoEntities.stream().map(todoEntity -> Todo.builder()
                .task(todoEntity.task)
                .active(todoEntity.active)
                .build()).collect(Collectors.toList());
        return todos;
    }
}
