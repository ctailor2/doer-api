package com.doerapispring.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MasterList implements UniquelyIdentifiable<String> {
    private final UniqueIdentifier<String> uniqueIdentifier;
    private final TodoList immediateList;
    private final TodoList postponedList;

    public MasterList(UniqueIdentifier<String> uniqueIdentifier, TodoList immediateList, TodoList postponedList) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.immediateList = immediateList;
        this.postponedList = postponedList;
    }

    public TodoList getImmediateList() {
        return immediateList;
    }

    public TodoList getPostponedList() {
        return postponedList;
    }

    @Override
    public UniqueIdentifier<String> getIdentifier() {
        return uniqueIdentifier;
    }

    public List<Todo> getTodos() {
        ArrayList<Todo> todos = new ArrayList<>();
        todos.addAll(immediateList.getTodos());
        todos.addAll(postponedList.getTodos());
        return todos;
    }

    public Todo add(String task, ScheduledFor scheduling) throws ListSizeExceededException, DuplicateTodoException {
        if (getByTask(task).isPresent()) throw new DuplicateTodoException();
        return getListByScheduling(scheduling).add(task);
    }

    List<Todo> pull() throws NoSourceListConfiguredException {
        return immediateList.pull();
    }

    Todo delete(String localIdentifier) throws TodoNotFoundException {
        Todo todoToDelete = getByLocalIdentifier(localIdentifier);
        getListByScheduling(todoToDelete.getScheduling()).remove(todoToDelete);
        return todoToDelete;
    }

    List<Todo> displace(String localIdentifier, String task) throws TodoNotFoundException, DuplicateTodoException, NoSourceListConfiguredException {
        if (getByTask(task).isPresent()) throw new DuplicateTodoException();
        Todo existingTodo = getByLocalIdentifier(localIdentifier);
        TodoList todoList = getListByScheduling(existingTodo.getScheduling());
        return todoList.displace(existingTodo, task);
    }

    Todo update(String localIdentifier, String task) throws TodoNotFoundException, DuplicateTodoException {
        if (getByTask(task).isPresent()) throw new DuplicateTodoException();
        Todo existingTodo = getByLocalIdentifier(localIdentifier);
        existingTodo.setTask(task);
        return existingTodo;
    }

    Todo complete(String localIdentifier) throws TodoNotFoundException {
        Todo existingTodo = getByLocalIdentifier(localIdentifier);
        getListByScheduling(existingTodo.getScheduling()).remove(existingTodo);
        existingTodo.complete();
        return existingTodo;
    }

    List<Todo> move(String originalTodoIdentifier, String targetTodoIdentifier) throws TodoNotFoundException {
        Todo originalTodo = getByLocalIdentifier(originalTodoIdentifier);
        TodoList todoList = getListByScheduling(originalTodo.getScheduling());
        Todo targetTodo = todoList.getByIdentifier(targetTodoIdentifier);
        return todoList.move(originalTodo, targetTodo);
    }

    TodoList getListByScheduling(ScheduledFor scheduling) {
        if (ScheduledFor.now.equals(scheduling)) {
            return immediateList;
        }
        return postponedList;
    }

    private Todo getByLocalIdentifier(String localIdentifier) throws TodoNotFoundException {
        return getTodos().stream()
                .filter(todo -> localIdentifier.equals(todo.getLocalIdentifier()))
                .findFirst()
                .orElseThrow(TodoNotFoundException::new);
    }

    private Optional<Todo> getByTask(String task) {
        return getTodos().stream().filter(todo ->
                todo.getTask().equals(task))
                .findFirst();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MasterList that = (MasterList) o;

        if (uniqueIdentifier != null ? !uniqueIdentifier.equals(that.uniqueIdentifier) : that.uniqueIdentifier != null)
            return false;
        if (immediateList != null ? !immediateList.equals(that.immediateList) : that.immediateList != null)
            return false;
        return postponedList != null ? postponedList.equals(that.postponedList) : that.postponedList == null;

    }

    @Override
    public int hashCode() {
        int result = uniqueIdentifier != null ? uniqueIdentifier.hashCode() : 0;
        result = 31 * result + (immediateList != null ? immediateList.hashCode() : 0);
        result = 31 * result + (postponedList != null ? postponedList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MasterList{" +
                "uniqueIdentifier=" + uniqueIdentifier +
                ", immediateList=" + immediateList +
                ", postponedList=" + postponedList +
                '}';
    }
}
