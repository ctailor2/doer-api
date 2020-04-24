package com.doerapispring.domain;

import com.doerapispring.domain.events.*;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.lang.String.format;

public class TodoListCommandModel implements DomainModel {
    private final Clock clock;
    private final UserId userId;
    private final List<Todo> todos;
    private final ListId listId;
    private final String name;
    private int version;
    private Date lastUnlockedAt;
    private Integer demarcationIndex;
    private List<TodoListEvent> domainEvents = new ArrayList<>();

    public TodoListCommandModel(
            Clock clock,
            UserId userId,
            ListId listId,
            String name,
            Date lastUnlockedAt,
            List<Todo> todos,
            Integer demarcationIndex) {
        this.clock = clock;
        this.userId = userId;
        this.listId = listId;
        this.name = name;
        this.lastUnlockedAt = lastUnlockedAt;
        this.todos = todos;
        this.demarcationIndex = demarcationIndex;
        this.version = 0;
    }

    private TodoListCommandModel(Clock clock, TodoList todoList) {
        this.clock = clock;
        this.userId = todoList.getUserId();
        this.listId = todoList.getListId();
        this.name = todoList.getName();
        this.demarcationIndex = 0;
        this.lastUnlockedAt = Date.from(Instant.EPOCH);
        this.todos = new ArrayList<>();
        this.version = 0;
    }

    public static TodoListCommandModel newInstance(Clock clock, TodoList todoList) {
        return new TodoListCommandModel(clock, todoList);
    }

    public TodoListCommandModel withEvents(List<DomainEvent> domainEvents) {
        domainEvents.forEach(this::applyEvent);
        clearDomainEvents();
        this.version = domainEvents.size();
        return this;
    }

    private void applyEvent(DomainEvent domainEvent) {
        try {
            String domainEventClassName = domainEvent.getClass().getName();
            switch (domainEventClassName) {
                case "com.doerapispring.domain.events.TodoAddedEvent":
                    handleEvent((TodoAddedEvent) domainEvent);
                    break;
                case "com.doerapispring.domain.events.DeferredTodoAddedEvent":
                    handleEvent((DeferredTodoAddedEvent) domainEvent);
                    break;
                case "com.doerapispring.domain.events.TodoDeletedEvent":
                    handleEvent((TodoDeletedEvent) domainEvent);
                    break;
                case "com.doerapispring.domain.events.PulledEvent":
                    handleEvent((PulledEvent) domainEvent);
                    break;
                case "com.doerapispring.domain.events.EscalatedEvent":
                    handleEvent((EscalatedEvent) domainEvent);
                    break;
                case "com.doerapispring.domain.events.TodoDisplacedEvent":
                    handleEvent((TodoDisplacedEvent) domainEvent);
                    break;
                case "com.doerapispring.domain.events.TodoMovedEvent":
                    handleEvent((TodoMovedEvent) domainEvent);
                    break;
                case "com.doerapispring.domain.events.TodoCompletedEvent":
                    handleEvent((TodoCompletedEvent) domainEvent);
                    break;
                case "com.doerapispring.domain.events.TodoUpdatedEvent":
                    handleEvent((TodoUpdatedEvent) domainEvent);
                    break;
                case "com.doerapispring.domain.events.UnlockedEvent":
                    handleEvent((UnlockedEvent) domainEvent);
                    break;
                default:
                    throw new IllegalArgumentException(format("Received unhandled domain event with class name: %s", domainEventClassName));
            }
        } catch (DuplicateTodoException | ListSizeExceededException | TodoNotFoundException | EscalateNotAllowException | ListNotFullException | LockTimerNotExpiredException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void add(TodoId todoId, String task) {
        handleEvent(new TodoAddedEvent(userId.get(), listId.get(), todoId.getIdentifier(), task));
    }

    private void handleEvent(TodoAddedEvent todoAddedEvent) {
        if (alreadyExists(todoAddedEvent.task())) {
            throw new DuplicateTodoException();
        }
        if (read().isFull()) {
            throw new ListSizeExceededException();
        }
        Todo todo = new Todo(new TodoId(todoAddedEvent.todoId()), todoAddedEvent.task());
        todos.add(0, todo);
        demarcationIndex++;
        this.domainEvents.add(todoAddedEvent);
    }

    public void addDeferred(TodoId todoId, String task) {
        handleEvent(new DeferredTodoAddedEvent(userId.get(), listId.get(), todoId.getIdentifier(), task));
    }

    private void handleEvent(DeferredTodoAddedEvent deferredTodoAddedEvent) {
        if (alreadyExists(deferredTodoAddedEvent.task())) {
            throw new DuplicateTodoException();
        }
        Todo todo = new Todo(new TodoId(deferredTodoAddedEvent.todoId()), deferredTodoAddedEvent.task());
        todos.add(todo);
        this.domainEvents.add(deferredTodoAddedEvent);
    }

    public void unlock() {
        handleEvent(new UnlockedEvent(
                userId.get(),
                listId.get(),
                Date.from(clock.instant())));
    }

    private void handleEvent(UnlockedEvent unlockedEvent) {
        if (!read().isAbleToBeUnlocked()) {
            throw new LockTimerNotExpiredException();
        }
        lastUnlockedAt = unlockedEvent.unlockedAt();
        this.domainEvents.add(unlockedEvent);
    }

    public void delete(TodoId todoId) {
        handleEvent(new TodoDeletedEvent(userId.get(), listId.get(), todoId.getIdentifier()));
    }

    private void handleEvent(TodoDeletedEvent todoDeletedEvent) {
        String todoId = todoDeletedEvent.todoId();
        doDelete(todoId);
        this.domainEvents.add(todoDeletedEvent);
    }

    private void doDelete(String todoId) {
        Todo todoToDelete = getByTodoId(new TodoId(todoId));
        if (todos.indexOf(todoToDelete) < demarcationIndex) {
            demarcationIndex--;
        }
        todos.remove(todoToDelete);
    }

    public void displace(TodoId todoId, String task) {
        handleEvent(new TodoDisplacedEvent(userId.get(), listId.get(), todoId.getIdentifier(), task));
    }

    private void handleEvent(TodoDisplacedEvent todoDisplacedEvent) {
        if (!read().isFull()) throw new ListNotFullException();
        if (alreadyExists(todoDisplacedEvent.task())) throw new DuplicateTodoException();
        Todo todo = new Todo(new TodoId(todoDisplacedEvent.todoId()), todoDisplacedEvent.task());
        todos.add(0, todo);
        this.domainEvents.add(todoDisplacedEvent);
    }

    public void update(TodoId todoId, String task) {
        handleEvent(new TodoUpdatedEvent(userId.get(), listId.get(), todoId.getIdentifier(), task));
    }

    private void handleEvent(TodoUpdatedEvent todoUpdatedEvent) {
        if (alreadyExists(todoUpdatedEvent.task())) {
            throw new DuplicateTodoException();
        }
        Todo todo = getByTodoId(new TodoId(todoUpdatedEvent.todoId()));
        todo.setTask(todoUpdatedEvent.task());
        this.domainEvents.add(todoUpdatedEvent);
    }

    public void complete(TodoId todoId) {
        Todo todo = getByTodoId(todoId);
        TodoCompletedEvent todoCompletedEvent = new TodoCompletedEvent(
                userId.get(),
                listId.get(),
                todoId.getIdentifier(),
                todo.getTask(),
                Date.from(clock.instant()));
        handleEvent(todoCompletedEvent);
    }

    private void handleEvent(TodoCompletedEvent todoCompletedEvent) {
        doDelete(todoCompletedEvent.completedTodoId());
        domainEvents.add(todoCompletedEvent);
    }

    public void move(TodoId todoId, TodoId targetTodoId) {
        handleEvent(new TodoMovedEvent(
                userId.get(),
                listId.get(),
                todoId.getIdentifier(),
                targetTodoId.getIdentifier()));
    }

    private void handleEvent(TodoMovedEvent todoMovedEvent) {
        Todo todo = getByTodoId(new TodoId(todoMovedEvent.todoId()));
        Todo targetTodo = getByTodoId(new TodoId(todoMovedEvent.targetTodoId()));
        int targetIndex = todos.indexOf(targetTodo);

        todos.remove(todo);
        todos.add(targetIndex, todo);
        this.domainEvents.add(todoMovedEvent);
    }

    public void pull() {
        handleEvent(new PulledEvent(userId.get(), listId.get()));
    }

    private void handleEvent(PulledEvent pulledEvent) {
        while (demarcationIndex < todos.size() && !read().isFull()) {
            demarcationIndex++;
        }
        this.domainEvents.add(pulledEvent);
    }

    public UserId getUserId() {
        return userId;
    }

    public ListId getListId() {
        return listId;
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    private Todo getByTodoId(TodoId todoId) {
        return todos.stream()
                .filter(todo -> todoId.equals(todo.getTodoId()))
                .findFirst()
                .orElseThrow(TodoNotFoundException::new);
    }

    private boolean alreadyExists(String task) {
        return todos.stream().anyMatch(todo -> todo.getTask().equals(task));
    }

    TodoListReadModel read() {
        return new TodoListReadModel(clock, name, lastUnlockedAt, todos, demarcationIndex, listId, userId);
    }

    public void escalate() {
        handleEvent(new EscalatedEvent(userId.get(), listId.get()));
    }

    private void handleEvent(EscalatedEvent escalatedEvent) {
        if (!read().isAbleToBeEscalated()) {
            throw new EscalateNotAllowException();
        }
        Todo firstDeferredTodo = todos.get(demarcationIndex);
        todos.remove(firstDeferredTodo);
        todos.add(demarcationIndex - 1, firstDeferredTodo);
        this.domainEvents.add(escalatedEvent);
    }

    @Override
    public List<TodoListEvent> getDomainEvents() {
        return domainEvents;
    }

    @Override
    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
