package com.doerapispring.domain;

import com.doerapispring.domain.events.*;

import java.util.*;

import static java.lang.String.format;

public class CompletedTodoList {
    private final UserId userId;
    private final ListId listId;
    private final List<CompletedTodo> todos;
    private Map<String, String> addedTodos = new HashMap<>();

    public CompletedTodoList(UserId userId,
                             ListId listId,
                             List<CompletedTodo> todos) {
        this.userId = userId;
        this.listId = listId;
        this.todos = todos;
    }

    public CompletedTodoList(UserId userId, ListId listId) {
        this.userId = userId;
        this.listId = listId;
        this.todos = new ArrayList<>();
    }

    public List<CompletedTodo> getTodos() {
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
            case "com.doerapispring.domain.events.TodoAddedEvent":
                handleEvent((TodoAddedEvent) domainEvent);
                break;
            case "com.doerapispring.domain.events.DeferredTodoAddedEvent":
                handleEvent((DeferredTodoAddedEvent) domainEvent);
                break;
            case "com.doerapispring.domain.events.TodoCompletedEvent":
                handleEvent((TodoCompletedEvent) domainEvent, timestampedDomainEvent.date());
                break;
            default:
                throw new IllegalArgumentException(format("Received unhandled domain event with class name: %s", domainEventClassName));
        }
    }

    private void handleEvent(TodoCompletedEvent todoCompletedEvent, Date date) {
        String todoId = todoCompletedEvent.completedTodoId();
        this.todos.add(0, new CompletedTodo(
                new CompletedTodoId(todoId),
                this.addedTodos.get(todoId),
                date));
    }

    private void handleEvent(DeferredTodoAddedEvent deferredTodoAddedEvent) {
        this.addedTodos.put(deferredTodoAddedEvent.todoId(), deferredTodoAddedEvent.task());
    }

    private void handleEvent(TodoAddedEvent todoAddedEvent) {
        this.addedTodos.put(todoAddedEvent.todoId(), todoAddedEvent.task());
    }
}
