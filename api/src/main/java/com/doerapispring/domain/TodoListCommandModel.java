package com.doerapispring.domain;

import com.doerapispring.domain.events.*;

import java.time.Clock;
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
        this.demarcationIndex = todoList.getDemarcationIndex();
        this.lastUnlockedAt = todoList.getLastUnlockedAt();
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
            DomainEventType domainEventType = domainEvent.type();
            switch (domainEventType) {
                case TODO_ADDED:
                    handleEvent((TodoAddedEvent) domainEvent);
                    break;
                case DEFERRED_TODO_ADDED:
                    handleEvent((DeferredTodoAddedEvent) domainEvent);
                    break;
                case TODO_DELETED:
                    handleEvent((TodoDeletedEvent) domainEvent);
                    break;
                case PULLED:
                    handleEvent((PulledEvent) domainEvent);
                    break;
                case ESCALATED:
                    handleEvent((EscalatedEvent) domainEvent);
                    break;
                case TODO_DISPLACED:
                    handleEvent((TodoDisplacedEvent) domainEvent);
                    break;
                case TODO_MOVED:
                    handleEvent((TodoMovedEvent) domainEvent);
                    break;
                case TODO_COMPLETED:
                    handleEvent((TodoCompletedEvent) domainEvent);
                    break;
                case TODO_UPDATED:
                    handleEvent((TodoUpdatedEvent) domainEvent);
                    break;
                case UNLOCKED:
                    handleEvent((UnlockedEvent) domainEvent);
                    break;
                default:
                    throw new IllegalArgumentException(format("Received unhandled domain event with type: %s", domainEvent.type()));
            }
        } catch (DuplicateTodoException | ListSizeExceededException | TodoNotFoundException | EscalateNotAllowException | ListNotFullException | LockTimerNotExpiredException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void add(TodoId todoId, String task) throws ListSizeExceededException, DuplicateTodoException {
        handleEvent(new TodoAddedEvent(userId.get(), listId.get(), todoId.getIdentifier(), task));
    }

    private void handleEvent(TodoAddedEvent todoAddedEvent) throws DuplicateTodoException, ListSizeExceededException {
        if (alreadyExists(todoAddedEvent.getTask())) {
            throw new DuplicateTodoException();
        }
        if (read().isFull()) {
            throw new ListSizeExceededException();
        }
        Todo todo = new Todo(new TodoId(todoAddedEvent.getTodoId()), todoAddedEvent.getTask());
        todos.add(0, todo);
        demarcationIndex++;
        this.domainEvents.add(todoAddedEvent);
    }

    public void addDeferred(TodoId todoId, String task) throws DuplicateTodoException {
        handleEvent(new DeferredTodoAddedEvent(userId.get(), listId.get(), todoId.getIdentifier(), task));
    }

    private void handleEvent(DeferredTodoAddedEvent deferredTodoAddedEvent) throws DuplicateTodoException {
        if (alreadyExists(deferredTodoAddedEvent.getTask())) {
            throw new DuplicateTodoException();
        }
        Todo todo = new Todo(new TodoId(deferredTodoAddedEvent.getTodoId()), deferredTodoAddedEvent.getTask());
        todos.add(todo);
        this.domainEvents.add(deferredTodoAddedEvent);
    }

    public void unlock() throws LockTimerNotExpiredException {
        handleEvent(new UnlockedEvent(
                userId.get(),
                listId.get(),
                Date.from(clock.instant())));
    }

    private void handleEvent(UnlockedEvent unlockedEvent) throws LockTimerNotExpiredException {
        if (!read().isAbleToBeUnlocked()) {
            throw new LockTimerNotExpiredException();
        }
        lastUnlockedAt = unlockedEvent.getUnlockedAt();
        this.domainEvents.add(unlockedEvent);
    }

    public void delete(TodoId todoId) throws TodoNotFoundException {
        handleEvent(new TodoDeletedEvent(userId.get(), listId.get(), todoId.getIdentifier()));
    }

    private void handleEvent(TodoDeletedEvent todoDeletedEvent) throws TodoNotFoundException {
        String todoId = todoDeletedEvent.getTodoId();
        doDelete(todoId);
        this.domainEvents.add(todoDeletedEvent);
    }

    private void doDelete(String todoId) throws TodoNotFoundException {
        Todo todoToDelete = getByTodoId(new TodoId(todoId));
        if (todos.indexOf(todoToDelete) < demarcationIndex) {
            demarcationIndex--;
        }
        todos.remove(todoToDelete);
    }

    public void displace(TodoId todoId, String task) throws DuplicateTodoException, ListNotFullException {
        handleEvent(new TodoDisplacedEvent(userId.get(), listId.get(), todoId.getIdentifier(), task));
    }

    private void handleEvent(TodoDisplacedEvent todoDisplacedEvent) throws ListNotFullException, DuplicateTodoException {
        if (!read().isFull()) throw new ListNotFullException();
        if (alreadyExists(todoDisplacedEvent.getTask())) throw new DuplicateTodoException();
        Todo todo = new Todo(new TodoId(todoDisplacedEvent.getTodoId()), todoDisplacedEvent.getTask());
        todos.add(0, todo);
        this.domainEvents.add(todoDisplacedEvent);
    }

    public void update(TodoId todoId, String task) throws TodoNotFoundException, DuplicateTodoException {
        handleEvent(new TodoUpdatedEvent(userId.get(), listId.get(), todoId.getIdentifier(), task));
    }

    private void handleEvent(TodoUpdatedEvent todoUpdatedEvent) throws TodoNotFoundException, DuplicateTodoException {
        if (alreadyExists(todoUpdatedEvent.getTask())) {
            throw new DuplicateTodoException();
        }
        Todo todo = getByTodoId(new TodoId(todoUpdatedEvent.getTodoId()));
        todo.setTask(todoUpdatedEvent.getTask());
        this.domainEvents.add(todoUpdatedEvent);
    }

    public void complete(TodoId todoId) throws TodoNotFoundException {
        Todo todo = getByTodoId(todoId);
        TodoCompletedEvent todoCompletedEvent = new TodoCompletedEvent(
                userId.get(),
                listId.get(),
                todoId.getIdentifier(),
                todo.getTask(),
                Date.from(clock.instant()));
        handleEvent(todoCompletedEvent);
    }

    private void handleEvent(TodoCompletedEvent todoCompletedEvent) throws TodoNotFoundException {
        doDelete(todoCompletedEvent.getCompletedTodoId());
        domainEvents.add(todoCompletedEvent);
    }

    public void move(TodoId todoId, TodoId targetTodoId) throws TodoNotFoundException {
        handleEvent(new TodoMovedEvent(
                userId.get(),
                listId.get(),
                todoId.getIdentifier(),
                targetTodoId.getIdentifier()));
    }

    private void handleEvent(TodoMovedEvent todoMovedEvent) throws TodoNotFoundException {
        Todo todo = getByTodoId(new TodoId(todoMovedEvent.getTodoId()));
        Todo targetTodo = getByTodoId(new TodoId(todoMovedEvent.getTargetTodoId()));
        int targetIndex = todos.indexOf(targetTodo);

        todos.remove(todo);
        todos.add(targetIndex, todo);
        this.domainEvents.add(todoMovedEvent);
    }

    public Integer getDemarcationIndex() {
        return demarcationIndex;
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

    private Todo getByTodoId(TodoId todoId) throws TodoNotFoundException {
        return todos.stream()
                .filter(todo -> todoId.equals(todo.getTodoId()))
                .findFirst()
                .orElseThrow(TodoNotFoundException::new);
    }

    private boolean alreadyExists(String task) {
        return todos.stream().anyMatch(todo -> todo.getTask().equals(task));
    }

    public List<Todo> getAllTodos() {
        return todos;
    }

    public Date getLastUnlockedAt() {
        return lastUnlockedAt;
    }

    TodoListReadModel read() {
        return new TodoListReadModel(clock, name, lastUnlockedAt, todos, demarcationIndex, listId, userId);
    }

    public void escalate() throws EscalateNotAllowException {
        handleEvent(new EscalatedEvent(userId.get(), listId.get()));
    }

    private void handleEvent(EscalatedEvent escalatedEvent) throws EscalateNotAllowException {
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
