package com.doerapispring.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class MasterList implements UniquelyIdentifiable<String> {
    private final UniqueIdentifier<String> uniqueIdentifier;
    private final Integer focusSize;
    private final TodoList immediateList;
    private final TodoList postponedList;

    public MasterList(UniqueIdentifier<String> uniqueIdentifier,
                      int focusSize,
                      List<Todo> todos) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.focusSize = focusSize;
        Map<Boolean, List<Todo>> partitionedTodos = todos.stream()
                .collect(Collectors.partitioningBy(todo ->
                        ScheduledFor.now.equals(todo.getScheduling())));
        immediateList = new TodoList(ScheduledFor.now, partitionedTodos.get(true), focusSize);
        postponedList = new TodoList(ScheduledFor.later, partitionedTodos.get(false), -1);
    }

    public MasterList(UniqueIdentifier<String> uniqueIdentifier, TodoList immediateList, TodoList postponedList) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.immediateList = immediateList;
        this.focusSize = immediateList.getMaxSize();
        this.postponedList = postponedList;
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

    private boolean isImmediateListFull() {
        return immediateList.isFull();
    }

    public Todo add(String task, ScheduledFor scheduling) throws ListSizeExceededException, DuplicateTodoException {
        if (ScheduledFor.now.equals(scheduling) && isImmediateListFull()) {
            throw new ListSizeExceededException();
        }
        if (getByTask(task).isPresent()) throw new DuplicateTodoException();
        return getListByScheduling(scheduling).add(task);
    }

    List<Todo> pull() {
        List<Todo> laterTodos = postponedList.pop(focusSize - immediateList.getTodos().size());
        return laterTodos.stream()
                .map(todo ->
                        immediateList.addExisting(todo.getLocalIdentifier(), todo.getTask()))
                .collect(Collectors.toList());
    }

    Todo delete(String localIdentifier) throws TodoNotFoundException {
        Todo todoToDelete = getByLocalIdentifier(localIdentifier);
        getListByScheduling(todoToDelete.getScheduling()).remove(todoToDelete);
        return todoToDelete;
    }

    List<Todo> displace(String localIdentifier, String task) throws TodoNotFoundException, DuplicateTodoException {
        if (getByTask(task).isPresent()) throw new DuplicateTodoException();
        Todo existingTodo = getByLocalIdentifier(localIdentifier);
        Todo replacementTodo = new Todo(existingTodo.getLocalIdentifier(), task, existingTodo.getScheduling(), existingTodo.getPosition());
        // TODO: maybe push this ^^ logic into the replace method??
        immediateList.replace(existingTodo, replacementTodo);
        Todo displacedTodo = postponedList.push(existingTodo.getTask());
        return asList(displacedTodo, replacementTodo);
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
        if (focusSize != null ? !focusSize.equals(that.focusSize) : that.focusSize != null) return false;
        if (immediateList != null ? !immediateList.equals(that.immediateList) : that.immediateList != null)
            return false;
        return postponedList != null ? postponedList.equals(that.postponedList) : that.postponedList == null;

    }

    @Override
    public int hashCode() {
        int result = uniqueIdentifier != null ? uniqueIdentifier.hashCode() : 0;
        result = 31 * result + (focusSize != null ? focusSize.hashCode() : 0);
        result = 31 * result + (immediateList != null ? immediateList.hashCode() : 0);
        result = 31 * result + (postponedList != null ? postponedList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MasterList{" +
                "uniqueIdentifier=" + uniqueIdentifier +
                ", focusSize=" + focusSize +
                ", immediateList=" + immediateList +
                ", postponedList=" + postponedList +
                '}';
    }
}
