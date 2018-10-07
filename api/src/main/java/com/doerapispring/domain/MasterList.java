package com.doerapispring.domain;

import java.time.Clock;
import java.util.Date;
import java.util.List;

public class MasterList extends ReadOnlyMasterList implements UniquelyIdentifiable<String> {
    public MasterList(Clock clock, UniqueIdentifier<String> uniqueIdentifier, Date lastUnlockedAt, List<Todo> todos, Integer demarcationIndex) {
        super(clock, uniqueIdentifier, lastUnlockedAt, todos, demarcationIndex);
    }

    public void add(TodoId todoId, String task) throws ListSizeExceededException, DuplicateTodoException {
        if (alreadyExists(task)) {
            throw new DuplicateTodoException();
        }
        if (isFull()) {
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
        if (!isAbleToBeUnlocked()) {
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
        if (!isFull()) throw new ListNotFullException();
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

    public String complete(TodoId todoId) throws TodoNotFoundException {
        Todo todo = getByTodoId(todoId);
        delete(todoId);
        return todo.getTask();
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
        while (demarcationIndex < todos.size() && !isFull()) {
            demarcationIndex++;
        }
    }

    public UniqueIdentifier<String> getIdentifier() {
        return uniqueIdentifier;
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

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MasterList that = (MasterList) o;

        if (demarcationIndex != that.demarcationIndex) return false;
        if (todos != null ? !todos.equals(that.todos) : that.todos != null) return false;
        if (clock != null ? !clock.equals(that.clock) : that.clock != null) return false;
        if (uniqueIdentifier != null ? !uniqueIdentifier.equals(that.uniqueIdentifier) : that.uniqueIdentifier != null)
            return false;
        return lastUnlockedAt != null ? lastUnlockedAt.equals(that.lastUnlockedAt) : that.lastUnlockedAt == null;
    }

    public int hashCode() {
        int result = todos != null ? todos.hashCode() : 0;
        result = 31 * result + (clock != null ? clock.hashCode() : 0);
        result = 31 * result + (uniqueIdentifier != null ? uniqueIdentifier.hashCode() : 0);
        result = 31 * result + demarcationIndex;
        result = 31 * result + (lastUnlockedAt != null ? lastUnlockedAt.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "MasterList{" +
            "todos=" + todos +
            ", clock=" + clock +
            ", uniqueIdentifier=" + uniqueIdentifier +
            ", demarcationIndex=" + demarcationIndex +
            ", lastUnlockedAt=" + lastUnlockedAt +
            '}';
    }
}
