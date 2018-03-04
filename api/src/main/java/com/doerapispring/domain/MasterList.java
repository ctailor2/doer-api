package com.doerapispring.domain;

import java.time.Clock;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;

public class MasterList implements UniquelyIdentifiable<String> {
    private static final long UNLOCK_DURATION_SECONDS = 1800L;
    public static final String NAME = "now";
    public static final String DEFERRED_NAME = "later";

    private final Clock clock;
    private final UniqueIdentifier<String> uniqueIdentifier;
    private final List<ListUnlock> listUnlocks;
    private final List<Todo> todos;
    private final List<Todo> deferredTodos;

    public MasterList(Clock clock, UniqueIdentifier<String> uniqueIdentifier, List<ListUnlock> listUnlocks) {
        this.clock = clock;
        this.uniqueIdentifier = uniqueIdentifier;
        this.listUnlocks = listUnlocks;

        todos = new ArrayList<>();
        deferredTodos = new ArrayList<>();
    }

    public MasterList(Clock clock, UniqueIdentifier<String> uniqueIdentifier, List<Todo> todos, List<Todo> deferredTodos, List<ListUnlock> listUnlocks) {
        this.clock = clock;
        this.uniqueIdentifier = uniqueIdentifier;
        this.todos = todos;
        this.deferredTodos = deferredTodos;
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
        if (isLocked()) throw new LockTimerNotExpiredException();
        return deferredTodos;
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
            getNextPosition(NAME));
        todos.add(todo);
        return todo;
    }

    private Integer getNextPosition(String listName) {
        Integer nextPosition;
        List<Todo> todos = getTodosByListName(listName);
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

    public Todo addDeferred(String task) throws DuplicateTodoException {
        if (getByTask(task).isPresent()) throw new DuplicateTodoException();
        Todo todo = new Todo(
            generateIdentifier(),
            task,
            DEFERRED_NAME,
            getNextPosition(DEFERRED_NAME));
        deferredTodos.add(todo);
        return todo;
    }

    public String getName() {
        return NAME;
    }

    public String getDeferredName() {
        return DEFERRED_NAME;
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
        return !isFull() && deferredTodos.size() > 0;
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

    List<Todo> pull() {
        int countOfTodosToPull = getMaxSize() - todos.size();
        List<Todo> sourceTodos = deferredTodos.stream()
            .limit(countOfTodosToPull)
            .collect(Collectors.toList());
        deferredTodos.removeAll(sourceTodos);
        ArrayList<Todo> pulledTodos = new ArrayList<>();
        for (Todo todo : sourceTodos) {
            Todo newTodo = new Todo(
                todo.getLocalIdentifier(),
                todo.getTask(),
                getName(),
                getNextPosition(NAME));
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
        Todo todo = new Todo(existingTodo.getLocalIdentifier(), existingTodo.getTask(), DEFERRED_NAME, getNextTopPosition(DEFERRED_NAME));
        deferredTodos.add(0, todo);
        return displacingTodo;
    }

    private int getNextTopPosition(String listName) {
        List<Todo> todos = getTodosByListName(listName);
        if (todos.isEmpty()) {
            return 1;
        }
        return todos.get(0).getPosition() - 1;
    }


    Todo update(String localIdentifier, String task) throws TodoNotFoundException, DuplicateTodoException {
        if (getByTask(task).isPresent()) throw new DuplicateTodoException();
        Todo existingTodo = getByLocalIdentifier(localIdentifier);
        existingTodo.setTask(task);
        return existingTodo;
    }

    Todo complete(String localIdentifier) throws TodoNotFoundException {
        Todo existingTodo = getByLocalIdentifier(localIdentifier);
        getTodosByListName(existingTodo.getListName()).remove(existingTodo);
        existingTodo.complete();
        return existingTodo;
    }

    List<Todo> move(String originalTodoIdentifier, String targetTodoIdentifier) throws TodoNotFoundException {
        Todo originalTodo = getByLocalIdentifier(originalTodoIdentifier);
        List<Todo> todos = getTodosByListName(originalTodo.getListName());
        Todo targetTodo = todos.stream()
            .filter(todo -> todo.getLocalIdentifier().equals(targetTodoIdentifier))
            .findFirst()
            .orElseThrow(TodoNotFoundException::new);

        Direction direction = Direction.valueOf(Integer.compare(targetTodo.getPosition(), originalTodo.getPosition()));

        int originalIndex = todos.indexOf(originalTodo);
        int targetIndex = todos.indexOf(targetTodo);

        Map<Integer, Integer> originalMapping = new HashMap<>();
        for (int index = originalIndex; direction.targetNotExceeded(index, targetIndex); index += direction.getValue()) {
            originalMapping.put(index, todos.get(index).getPosition());
        }

        todos.remove(originalTodo);
        todos.add(targetIndex, originalTodo);

        return originalMapping.entrySet().stream()
            .map(entry -> {
                Todo todo = todos.get(entry.getKey());
                todo.setPosition(entry.getValue());
                return todo;
            })
            .collect(Collectors.toList());
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
        if (listUnlocks != null ? !listUnlocks.equals(that.listUnlocks) : that.listUnlocks != null) return false;
        if (todos != null ? !todos.equals(that.todos) : that.todos != null) return false;
        return deferredTodos != null ? deferredTodos.equals(that.deferredTodos) : that.deferredTodos == null;
    }

    @Override
    public int hashCode() {
        int result = clock != null ? clock.hashCode() : 0;
        result = 31 * result + (uniqueIdentifier != null ? uniqueIdentifier.hashCode() : 0);
        result = 31 * result + (listUnlocks != null ? listUnlocks.hashCode() : 0);
        result = 31 * result + (todos != null ? todos.hashCode() : 0);
        result = 31 * result + (deferredTodos != null ? deferredTodos.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MasterList{" +
            "clock=" + clock +
            ", uniqueIdentifier=" + uniqueIdentifier +
            ", listUnlocks=" + listUnlocks +
            ", todos=" + todos +
            ", deferredTodos=" + deferredTodos +
            '}';
    }

    private List<Todo> getTodosByListName(String listName) {
        if (NAME.equals(listName)) {
            return todos;
        } else {
            return deferredTodos;
        }
    }

    private Optional<Todo> getByTask(String task) {
        return getAllTodos().stream().filter(todo ->
            todo.getTask().equals(task))
            .findFirst();
    }

    private List<Todo> getAllTodos() {
        ArrayList<Todo> allTodos = new ArrayList<>();
        allTodos.addAll(todos);
        allTodos.addAll(deferredTodos);
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
