package com.doerapispring.domain;

import com.doerapispring.domain.events.TodoListEvent;
import scala.Tuple2;
import scala.util.Try;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface TodoApplicationService {
    void performOperation(User user, ListId listId, Function<TodoListModel, Try<Tuple2<TodoListModel, TodoListEvent>>> operation);

    void performOperation(User user, ListId listId, BiFunction<TodoListModel, TodoId, Try<Tuple2<TodoListModel, TodoListEvent>>> operation);
}
