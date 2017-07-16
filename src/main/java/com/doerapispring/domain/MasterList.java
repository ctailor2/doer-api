package com.doerapispring.domain;

import java.time.Instant;
import java.util.*;

import static java.util.Arrays.asList;

public class MasterList implements UniquelyIdentifiable<String> {
    private final UniqueIdentifier<String> uniqueIdentifier;
    private final TodoList immediateList;
    private final TodoList postponedList;
    private List<ListUnlock> listUnlocks;

    public MasterList(UniqueIdentifier<String> uniqueIdentifier, TodoList immediateList, TodoList postponedList, List<ListUnlock> listUnlocks) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.immediateList = immediateList;
        this.postponedList = postponedList;
        this.listUnlocks = listUnlocks;
    }

    @Override
    public UniqueIdentifier<String> getIdentifier() {
        return uniqueIdentifier;
    }

    public List<ListUnlock> getListUnlocks() {
        return listUnlocks;
    }

    public List<Todo> getTodos() {
        return immediateList.getTodos();
    }

    public List<Todo> getDeferredTodos() {
        return postponedList.getTodos();
    }

    public List<Todo> getAllTodos() {
        ArrayList<Todo> todos = new ArrayList<>();
        todos.addAll(immediateList.getTodos());
        todos.addAll(postponedList.getTodos());
        return todos;
    }

    public Todo add(String task, ScheduledFor scheduling) throws ListSizeExceededException, DuplicateTodoException {
        if (getByTask(task).isPresent()) throw new DuplicateTodoException();
        return getListByScheduling(scheduling).add(task);
    }

    public boolean isLocked() {
        return getLastViewedAt()
            .map(lastViewedAt -> lastViewedAt.before(new Date(Instant.now().minusSeconds(1800L).toEpochMilli())))
            .orElse(true);
    }

    public String getName() {
        return immediateList.getScheduling().toString();
    }

    public String getDeferredName() {
        return postponedList.getScheduling().toString();
    }

    public boolean isFull() {
        return immediateList.isFull();
    }

    List<Todo> pull() throws ListSizeExceededException {
        List<Todo> sourceTodos = postponedList.pop(immediateList.getMaxSize() - immediateList.getTodos().size());
        ArrayList<Todo> pulledTodos = new ArrayList<>();
        for (Todo todo : sourceTodos) {
            Todo pulledTodo = immediateList.addExisting(todo.getLocalIdentifier(), todo.getTask());
            pulledTodos.add(pulledTodo);
        }
        return pulledTodos;
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
        // TODO: Replace behavior may not be necessary - see todo in tests for this
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

    ListUnlock unlock() throws LockTimerNotExpiredException {
        Boolean viewNotAllowed = getLastViewedAt()
            .map(lastViewedAt -> lastViewedAt.after(beginningOfToday()))
            .orElse(false);
        if (viewNotAllowed) {
            throw new LockTimerNotExpiredException();
        }
        return new ListUnlock();
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
        if (postponedList != null ? !postponedList.equals(that.postponedList) : that.postponedList != null)
            return false;
        return listUnlocks != null ? listUnlocks.equals(that.listUnlocks) : that.listUnlocks == null;

    }

    @Override
    public int hashCode() {
        int result = uniqueIdentifier != null ? uniqueIdentifier.hashCode() : 0;
        result = 31 * result + (immediateList != null ? immediateList.hashCode() : 0);
        result = 31 * result + (postponedList != null ? postponedList.hashCode() : 0);
        result = 31 * result + (listUnlocks != null ? listUnlocks.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MasterList{" +
            "uniqueIdentifier=" + uniqueIdentifier +
            ", immediateList=" + immediateList +
            ", postponedList=" + postponedList +
            ", listUnlocks=" + listUnlocks +
            '}';
    }

    private Todo getByLocalIdentifier(String localIdentifier) throws TodoNotFoundException {
        return getAllTodos().stream()
            .filter(todo -> localIdentifier.equals(todo.getLocalIdentifier()))
            .findFirst()
            .orElseThrow(TodoNotFoundException::new);
    }

    private Optional<Todo> getByTask(String task) {
        return getAllTodos().stream().filter(todo ->
            todo.getTask().equals(task))
            .findFirst();
    }

    private Optional<Date> getLastViewedAt() {
        return listUnlocks.stream()
            .findFirst()
            .map(ListUnlock::getCreatedAt);
    }

    private Date beginningOfToday() {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
