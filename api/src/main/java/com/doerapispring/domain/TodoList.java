package com.doerapispring.domain;

import java.time.Clock;
import java.util.Date;
import java.util.List;

public class TodoList {
    private final Clock clock;
    private final UserId userId;
    private final List<Todo> todos;
    private final ListId listId;
    private final String name;
    private Date lastUnlockedAt;
    private Integer demarcationIndex;

    public TodoList(
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
    }

    public void add(TodoId todoId, String task) throws ListSizeExceededException, DuplicateTodoException {
        if (alreadyExists(task)) {
            throw new DuplicateTodoException();
        }
        if (read().isFull()) {
            throw new ListSizeExceededException();
        }
        Todo todo = new Todo(todoId, task);
        todos.add(0, todo);
        demarcationIndex++;
    }

    public void addDeferred(TodoId todoId, String task) throws DuplicateTodoException {
        if (alreadyExists(task)) {
            throw new DuplicateTodoException();
        }
        Todo todo = new Todo(todoId, task);
        todos.add(todo);
    }

    public void unlock() throws LockTimerNotExpiredException {
        if (!read().isAbleToBeUnlocked()) {
            throw new LockTimerNotExpiredException();
        }
        lastUnlockedAt = Date.from(clock.instant());
    }

    public void delete(TodoId todoId) throws TodoNotFoundException {
        Todo todoToDelete = getByTodoId(todoId);
        if (todos.indexOf(todoToDelete) < demarcationIndex) {
            demarcationIndex--;
        }
        todos.remove(todoToDelete);
    }

    public void displace(TodoId todoId, String task) throws DuplicateTodoException, ListNotFullException {
        if (!read().isFull()) throw new ListNotFullException();
        if (alreadyExists(task)) throw new DuplicateTodoException();
        Todo todo = new Todo(todoId, task);
        todos.add(0, todo);
    }

    public void update(TodoId todoId, String task) throws TodoNotFoundException, DuplicateTodoException {
        if (alreadyExists(task)) {
            throw new DuplicateTodoException();
        }
        Todo todo = getByTodoId(todoId);
        todo.setTask(task);
    }

    public CompletedTodo complete(TodoId todoId) throws TodoNotFoundException {
        Todo todo = getByTodoId(todoId);
        delete(todoId);
        return new CompletedTodo(
            userId,
            new CompletedTodoId(todoId.getIdentifier()),
            todo.getTask(),
            Date.from(clock.instant()));
    }

    public void move(TodoId todoId, TodoId targetTodoId) throws TodoNotFoundException {
        Todo todo = getByTodoId(todoId);
        Todo targetTodo = getByTodoId(targetTodoId);
        int targetIndex = todos.indexOf(targetTodo);

        todos.remove(todo);
        todos.add(targetIndex, todo);
    }

    public Integer getDemarcationIndex() {
        return demarcationIndex;
    }

    public void pull() {
        while (demarcationIndex < todos.size() && !read().isFull()) {
            demarcationIndex++;
        }
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

    ReadOnlyTodoList read() {
        return new ReadOnlyTodoList(clock, name, lastUnlockedAt, todos, demarcationIndex, listId);
    }

    void escalate() throws EscalateNotAllowException {
        if (!read().isAbleToBeEscalated()) {
            throw new EscalateNotAllowException();
        }
        Todo firstDeferredTodo = todos.get(demarcationIndex);
        todos.remove(firstDeferredTodo);
        todos.add(demarcationIndex - 1, firstDeferredTodo);
    }
}
