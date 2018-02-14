package com.doerapispring.domain;

import java.time.Clock;
import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparingInt;

public class MasterList implements UniquelyIdentifiable<String> {
    private static final long UNLOCK_DURATION_SECONDS = 1800L;
    public static final String NAME = "now";
    public static final String DEFERRED_NAME = "later";

    private final Clock clock;
    private final UniqueIdentifier<String> uniqueIdentifier;
    private final TodoList immediateList;
    private final TodoList postponedList;
    private final List<ListUnlock> listUnlocks;

    private List<Todo> todos = new ArrayList<>();

    public MasterList(Clock clock, UniqueIdentifier<String> uniqueIdentifier, TodoList immediateList, TodoList postponedList, List<ListUnlock> listUnlocks) {
        this.clock = clock;
        this.uniqueIdentifier = uniqueIdentifier;
        this.immediateList = immediateList;
        this.postponedList = postponedList;
        this.listUnlocks = listUnlocks;
    }

    public MasterList(Clock clock, UniqueIdentifier<String> uniqueIdentifier, List<ListUnlock> listUnlocks) {
        this.clock = clock;
        this.uniqueIdentifier = uniqueIdentifier;
        this.immediateList = new TodoList(NAME, 2);
        this.postponedList = new TodoList(DEFERRED_NAME, -1);
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
        return todos;
    }

    public List<Todo> getDeferredTodos() throws LockTimerNotExpiredException {
        if (isLocked()) {
            throw new LockTimerNotExpiredException();
        }
        return postponedList.getTodos();
    }

    public Todo add(String task) throws ListSizeExceededException, DuplicateTodoException {
        if (getByTask(task).isPresent()) throw new DuplicateTodoException();
        if (isFull()) {
            throw new ListSizeExceededException();
        }
        Todo todo = new Todo(
            generateIdentifier(),
            task,
            getName(),
            getNextPosition());
        todos.add(todo);
        return todo;
    }

    private Integer getNextPosition() {
        Integer nextPosition;
        if (todos.isEmpty()) {
            nextPosition = 1;
        } else {
            nextPosition = todos.get(todos.size() - 1).getPosition() + 1;
        }
        return nextPosition;
    }

    private String generateIdentifier() {
        return UUID.randomUUID().toString();
    }

    public Todo addDeferred(String task) throws ListSizeExceededException, DuplicateTodoException {
        if (getByTask(task).isPresent()) throw new DuplicateTodoException();
        return postponedList.add(task);
    }

    public String getName() {
        return NAME;
    }

    public String getDeferredName() {
        return postponedList.getName();
    }

    public boolean isLocked() {
        return getLastUnlockedAt()
            .map(lastUnlockedAt -> lastUnlockedAt.before(new Date(clock.instant().minusSeconds(UNLOCK_DURATION_SECONDS).toEpochMilli())))
            .orElse(true);
    }

    public boolean isFull() {
        return todos.size() >= getMaxSize();
    }

    private int getMaxSize() {
        return 2;
    }

    public boolean isAbleToBeReplenished() {
        return !isFull() && postponedList.getTodos().size() > 0;
    }

    public Boolean isAbleToBeUnlocked() {
        return isLocked() && getLastUnlockedAt()
            .map(lastUnlockedAt -> lastUnlockedAt.before(beginningOfToday()))
            .orElse(true);
    }

    public Long unlockDuration() {
        return getLastUnlockedAt()
            .map(lastUnlockedAt -> {
                long unlockExpiration = lastUnlockedAt.toInstant().toEpochMilli() + UNLOCK_DURATION_SECONDS * 1000;
                long now = clock.instant().toEpochMilli();
                return unlockExpiration - now;
            })
            .filter(duration -> duration > 0L)
            .orElse(0L);
    }

    List<Todo> pull() throws ListSizeExceededException {
        List<Todo> sourceTodos = postponedList.pop(getMaxSize() - todos.size());
        ArrayList<Todo> pulledTodos = new ArrayList<>();
        for (Todo todo : sourceTodos) {
            Todo newTodo = new Todo(
                todo.getLocalIdentifier(),
                todo.getTask(),
                getName(),
                getNextPosition());
            todos.add(newTodo);
            pulledTodos.add(newTodo);
        }
        return pulledTodos;
    }

    Todo delete(String localIdentifier) throws TodoNotFoundException {
        Todo todoToDelete = getByLocalIdentifier(localIdentifier);
        getTodosByListName(todoToDelete.getListName()).remove(todoToDelete);
        return todoToDelete;
    }

    Todo displace(String localIdentifier, String task) throws TodoNotFoundException, DuplicateTodoException {
        if (getByTask(task).isPresent()) throw new DuplicateTodoException();
        Todo existingTodo = delete(localIdentifier);
        Todo displacingTodo = new Todo(
            generateIdentifier(),
            task,
            NAME,
            existingTodo.getPosition());
        todos.add(displacingTodo);
        todos.sort(comparingInt(Todo::getPosition));
        postponedList.pushExisting(existingTodo.getLocalIdentifier(), existingTodo.getTask());
        return displacingTodo;
    }

    Todo update(String localIdentifier, String task) throws TodoNotFoundException, DuplicateTodoException {
        if (getByTask(task).isPresent()) throw new DuplicateTodoException();
        Todo existingTodo = getByLocalIdentifier(localIdentifier);
        existingTodo.setTask(task);
        return existingTodo;
    }

    Todo complete(String localIdentifier) throws TodoNotFoundException {
        Todo existingTodo = getByLocalIdentifier(localIdentifier);
        getListByName(existingTodo.getListName()).remove(existingTodo);
        existingTodo.complete();
        return existingTodo;
    }

    List<Todo> move(String originalTodoIdentifier, String targetTodoIdentifier) throws TodoNotFoundException {
        Todo originalTodo = getByLocalIdentifier(originalTodoIdentifier);
        TodoList todoList = getListByName(originalTodo.getListName());
        Todo targetTodo = todoList.getByIdentifier(targetTodoIdentifier);
        return todoList.move(originalTodo, targetTodo);
    }

    ListUnlock unlock() throws LockTimerNotExpiredException {
        if (!isAbleToBeUnlocked()) {
            throw new LockTimerNotExpiredException();
        }
        ListUnlock listUnlock = new ListUnlock(Date.from(clock.instant()));
        listUnlocks.add(listUnlock);
        return listUnlock;
    }

    Todo getByLocalIdentifier(String localIdentifier) throws TodoNotFoundException {
        return getAllTodos().stream()
            .filter(todo -> localIdentifier.equals(todo.getLocalIdentifier()))
            .findFirst()
            .orElseThrow(TodoNotFoundException::new);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MasterList that = (MasterList) o;

        if (clock != null ? !clock.equals(that.clock) : that.clock != null) return false;
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
        int result = clock != null ? clock.hashCode() : 0;
        result = 31 * result + (uniqueIdentifier != null ? uniqueIdentifier.hashCode() : 0);
        result = 31 * result + (immediateList != null ? immediateList.hashCode() : 0);
        result = 31 * result + (postponedList != null ? postponedList.hashCode() : 0);
        result = 31 * result + (listUnlocks != null ? listUnlocks.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MasterList{" +
            "clock=" + clock +
            ", uniqueIdentifier=" + uniqueIdentifier +
            ", immediateList=" + immediateList +
            ", postponedList=" + postponedList +
            ", listUnlocks=" + listUnlocks +
            '}';
    }

    private TodoList getListByName(String listName) {
        if (NAME.equals(listName)) {
            return immediateList;
        }
        return postponedList;
    }

    private List<Todo> getTodosByListName(String listName) {
        if (NAME.equals(listName)) {
            return todos;
        }
        return emptyList();
    }

    private Optional<Todo> getByTask(String task) {
        return getAllTodos().stream().filter(todo ->
            todo.getTask().equals(task))
            .findFirst();
    }

    private List<Todo> getAllTodos() {
        ArrayList<Todo> allTodos = new ArrayList<>();
        allTodos.addAll(todos);
        allTodos.addAll(postponedList.getTodos());
        return allTodos;
    }

    private Optional<Date> getLastUnlockedAt() {
        return listUnlocks.stream()
            .findFirst()
            .map(ListUnlock::getCreatedAt);
    }

    private Date beginningOfToday() {
        Date now = Date.from(clock.instant());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone(clock.getZone()));
        calendar.setTime(now);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
