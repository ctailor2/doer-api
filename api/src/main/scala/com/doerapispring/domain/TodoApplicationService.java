package com.doerapispring.domain;

import com.doerapispring.domain.events.TodoListEvent;
import scala.util.Try;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface TodoApplicationService {
    TodoListModel performOperation(
            User user,
            ListId listId,
            TodoListEvent event
    );
}