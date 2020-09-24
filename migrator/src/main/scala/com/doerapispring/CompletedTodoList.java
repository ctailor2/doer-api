package com.doerapispring;

import com.doerapispring.domain.ListId;
import com.doerapispring.domain.UserId;
import com.doerapispring.domain.events.*;

import java.util.*;

import static java.lang.String.format;

public class CompletedTodoList {
    private final UserId userId;
    private final ListId listId;
    private final List<DeprecatedCompletedTodo> todos;
    private Map<String, String> addedTodos = new HashMap<>();

    public CompletedTodoList(UserId userId, ListId listId) {
        this.userId = userId;
        this.listId = listId;
        this.todos = new ArrayList<>();
    }

    public List<DeprecatedCompletedTodo> getTodos() {
        return todos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompletedTodoList that = (CompletedTodoList) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(listId, that.listId) &&
                Objects.equals(todos, that.todos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, listId, todos);
    }

    @Override
    public String toString() {
        return "CompletedTodoList{" +
                "userId=" + userId +
                ", listId=" + listId +
                ", todos=" + todos +
                '}';
    }

    public CompletedTodoList withEvents(List<TimestampedDomainEvent> timestampedDomainEvents) {
        timestampedDomainEvents.forEach(this::applyEvent);
        return this;
    }

    private void applyEvent(TimestampedDomainEvent timestampedDomainEvent) {
        DomainEvent domainEvent = timestampedDomainEvent.domainEvent();
        String domainEventClassName = domainEvent.getClass().getName();
        switch (domainEventClassName) {
            case "com.doerapispring.domain.events.DeprecatedTodoAddedEvent":
                handleEvent((DeprecatedTodoAddedEvent) domainEvent);
                break;
            case "com.doerapispring.domain.events.DeprecatedDeferredTodoAddedEvent":
                handleEvent((DeprecatedDeferredTodoAddedEvent) domainEvent);
                break;
            case "com.doerapispring.domain.events.DeprecatedTodoDisplacedEvent":
                handleEvent((DeprecatedTodoDisplacedEvent) domainEvent);
                break;
            case "com.doerapispring.domain.events.DeprecatedTodoCompletedEvent":
                handleEvent((DeprecatedTodoCompletedEvent) domainEvent, timestampedDomainEvent.date());
                break;
            default:
                throw new IllegalArgumentException(format("Received unhandled domain event with class name: %s", domainEventClassName));
        }
    }

    private void handleEvent(DeprecatedTodoCompletedEvent todoCompletedEvent, Date date) {
        String todoId = todoCompletedEvent.completedTodoId();
        this.todos.add(0, new DeprecatedCompletedTodo(
                new CompletedTodoId(todoId),
                this.addedTodos.get(todoId),
                date));
    }

    private void handleEvent(DeprecatedTodoDisplacedEvent todoDisplacedEvent) {
        this.addedTodos.put(todoDisplacedEvent.todoId(), todoDisplacedEvent.task());
    }

    private void handleEvent(DeprecatedDeferredTodoAddedEvent deferredTodoAddedEvent) {
        this.addedTodos.put(deferredTodoAddedEvent.todoId(), deferredTodoAddedEvent.task());
    }

    private void handleEvent(DeprecatedTodoAddedEvent todoAddedEvent) {
        this.addedTodos.put(todoAddedEvent.todoId(), todoAddedEvent.task());
    }
}
