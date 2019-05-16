package com.doerapispring.domain;

import com.doerapispring.web.InvalidRequestException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final ObjectRepository<User, UserId> userRepository;
    private final TodoListFactory todoListFactory;
    private final OwnedObjectRepository<TodoList, UserId, ListId> todoListRepository;

    UserService(ObjectRepository<User, UserId> userRepository,
                TodoListFactory todoListFactory,
                OwnedObjectRepository<TodoList, UserId, ListId> todoListRepository) {
        this.userRepository = userRepository;
        this.todoListFactory = todoListFactory;
        this.todoListRepository = todoListRepository;
    }

    public User create(String identifier) throws InvalidRequestException {
        UserId userId = new UserId(identifier);
        Optional<User> userOptional = userRepository.find(userId);
        if (userOptional.isPresent()) throw new InvalidRequestException();
        User user = new User(userId);
        userRepository.save(user);
        TodoList todoList = todoListFactory.todoList(
            userId,
            todoListRepository.nextIdentifier(),
            "default");
        todoListRepository.save(todoList);
        return user;
    }
}
