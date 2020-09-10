package com.doerapispring.domain;

import com.doerapispring.domain.events.TodoListEvent;
import scala.util.Try;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public interface ListApplicationService {
    Try<TodoListModel> performOperation(
            User user,
            ListId listId,
            TodoListEvent event);

    TodoListModel getDefault(User user);

    CompletedTodoList getCompleted(User user, ListId listId);

    TodoListModel get(User user, ListId listId);

    List<TodoList> getAll(User user);

    void create(User user, String name);

    void setDefault(User user, ListId listId);
}
