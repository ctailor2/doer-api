package com.doerapispring.todos;

import com.doerapispring.AbnormalModelException;
import com.doerapispring.UserIdentifier;
import com.doerapispring.userSessions.OperationRefusedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by chiragtailor on 9/27/16.
 */
@Service
@Transactional
public class TodoService {
    private final NewTodoRepository newTodoRepository;
    private final TodoDao todoDao;

    @Autowired
    public TodoService(NewTodoRepository newTodoRepository,
                       TodoDao todoDao) {
        this.newTodoRepository = newTodoRepository;
        this.todoDao = todoDao;
    }

    public NewTodo newCreate(UserIdentifier userIdentifier, String task, ScheduledFor scheduling) throws OperationRefusedException {
        NewTodo todo = new NewTodo(userIdentifier, task, scheduling);
        try {
            newTodoRepository.add(todo);
        } catch (AbnormalModelException e) {
            throw new OperationRefusedException();
        }
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
            todoEntities = todoDao.findByUserEmail(userEmail);
        } else {
            TodoTypeQueryEnum todoTypeQueryEnum = Enum.valueOf(TodoTypeQueryEnum.class, type.toString());
            todoEntities = todoDao.findByUserEmailAndType(userEmail, todoTypeQueryEnum.getValue());
        }
        List<Todo> todos = todoEntities.stream().map(todoEntity -> Todo.builder()
                .task(todoEntity.task)
                .active(todoEntity.active)
                .build()).collect(Collectors.toList());
        return todos;
    }
}
