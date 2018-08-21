package com.doerapispring.domain;

import java.time.Clock;
import java.util.*;

import static java.util.Collections.emptyList;

public class MasterList implements IMasterList, UniquelyIdentifiable<String> {
    private static final String NAME = "now";
    private static final String DEFERRED_NAME = "later";
    private static final long UNLOCK_DURATION = 1800000L;
    private static final int MAX_SIZE = 2;
    private final List<Todo> todos;
    private final Clock clock;
    private final UniqueIdentifier<String> uniqueIdentifier;

    private Integer demarcationIndex = 0;
    private Date lastUnlockedAt;

    public MasterList(Clock clock, UniqueIdentifier<String> uniqueIdentifier, Date lastUnlockedAt, List<Todo> todos, Integer demarcationIndex) {
        this.clock = clock;
        this.uniqueIdentifier = uniqueIdentifier;
        this.todos = todos;
        this.lastUnlockedAt = lastUnlockedAt;
        this.demarcationIndex = demarcationIndex;
    }

    @Override
    public void add(String task) throws ListSizeExceededException, DuplicateTodoException {
        if (alreadyExists(task)) {
            throw new DuplicateTodoException();
        }
        if (getTodos().size() >= MAX_SIZE) {
            throw new ListSizeExceededException();
        }
        Todo todo = new Todo(generateIdentifier(), task);
        todos.add(0, todo);
        demarcationIndex++;
    }

    @Override
    public List<Todo> getTodos() {
        return todos.subList(0, demarcationIndex);
    }

    @Override
    public void addDeferred(String task) throws DuplicateTodoException {
        if (alreadyExists(task)) {
            throw new DuplicateTodoException();
        }
        Todo todo = new Todo(generateIdentifier(), task);
        todos.add(todo);
    }

    @Override
    public void unlock() throws LockTimerNotExpiredException {
        if (!isAbleToBeUnlocked()) {
            throw new LockTimerNotExpiredException();
        }
        lastUnlockedAt = Date.from(clock.instant());
    }

    @Override
    public List<Todo> getDeferredTodos() {
        if (isLocked()) {
            return emptyList();
        }
        return deferredTodos();
    }

    @Override
    public void delete(String localIdentifier) throws TodoNotFoundException {
        Todo todoToDelete = getByLocalIdentifier(localIdentifier);
        if (todos.indexOf(todoToDelete) < demarcationIndex) {
            demarcationIndex--;
        }
        todos.remove(todoToDelete);
    }

    @Override
    public void displace(String task) throws DuplicateTodoException, ListNotFullException {
        if (!isFull()) throw new ListNotFullException();
        if (alreadyExists(task)) throw new DuplicateTodoException();
        Todo todo = new Todo(generateIdentifier(), task);
        todos.add(0, todo);
    }

    @Override
    public void update(String localIdentifier, String task) throws TodoNotFoundException, DuplicateTodoException {
        if (alreadyExists(task)) {
            throw new DuplicateTodoException();
        }
        Todo todo = getByLocalIdentifier(localIdentifier);
        todo.setTask(task);
    }

    @Override
    public String complete(String localIdentifier) throws TodoNotFoundException {
        Todo todo = getByLocalIdentifier(localIdentifier);
        delete(localIdentifier);
        return todo.getTask();
    }

    @Override
    public void move(String localIdentifier, String targetLocalIdentifier) throws TodoNotFoundException {
        Todo todo = getByLocalIdentifier(localIdentifier);
        int index = todos.indexOf(todo);
        Todo targetTodo = getByLocalIdentifier(targetLocalIdentifier);
        int targetIndex;
        List<Todo> subList;
        if (index < demarcationIndex) {
            subList = getTodos();
        } else {
            subList = deferredTodos();
        }
        targetIndex = subList.indexOf(targetTodo);

        subList.remove(todo);
        subList.add(targetIndex, todo);
    }

    @Override
    public boolean isAbleToBeUnlocked() {
        return isLocked() && mostRecentListUnlock()
            .map(listUnlock -> listUnlock.before(beginningOfToday()))
            .orElse(true);
    }

    @Override
    public boolean isLocked() {
        return mostRecentListUnlock()
            .map(listUnlock -> listUnlock.before(Date.from(clock.instant().minusSeconds(1800L))))
            .orElse(true);
    }

    @Override
    public Long unlockDuration() {
        return mostRecentListUnlock()
            .map(listUnlock -> listUnlock.toInstant().toEpochMilli() + UNLOCK_DURATION - clock.instant().toEpochMilli())
            .filter(duration -> duration > 0L)
            .orElse(0L);
    }

    @Override
    public String getTask(String localIdentifier) {
        return null;
    }

    @Override
    public Integer getDemarcationIndex() {
        return demarcationIndex;
    }

    @Override
    public void pull() {
        while (demarcationIndex < todos.size() && getTodos().size() < MAX_SIZE) {
            demarcationIndex++;
        }
    }

    @Override
    public boolean isFull() {
        return getTodos().size() >= MAX_SIZE;
    }

    @Override
    public boolean isAbleToBeReplenished() {
        return !isFull() && deferredTodos().size() > 0;
    }

    @Override
    public UniqueIdentifier<String> getIdentifier() {
        return uniqueIdentifier;
    }

    private Todo getByLocalIdentifier(String localIdentifier) throws TodoNotFoundException {
        return todos.stream()
            .filter(todo -> localIdentifier.equals(todo.getLocalIdentifier()))
            .findFirst()
            .orElseThrow(TodoNotFoundException::new);
    }

    private boolean alreadyExists(String task) {
        return todos.stream().anyMatch(todo -> todo.getTask().equals(task));
    }

    private List<Todo> deferredTodos() {
        return todos.subList(demarcationIndex, todos.size());
    }

    private String generateIdentifier() {
        return UUID.randomUUID().toString();
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

    private Optional<Date> mostRecentListUnlock() {
        return Optional.ofNullable(lastUnlockedAt);
    }

    String getName() {
        return NAME;
    }

    String getDeferredName() {
        return DEFERRED_NAME;
    }

    public List<Todo> getAllTodos() {
        return todos;
    }

    public Date getLastUnlockedAt() {
        return lastUnlockedAt;
    }

    @Override
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

    @Override
    public int hashCode() {
        int result = todos != null ? todos.hashCode() : 0;
        result = 31 * result + (clock != null ? clock.hashCode() : 0);
        result = 31 * result + (uniqueIdentifier != null ? uniqueIdentifier.hashCode() : 0);
        result = 31 * result + demarcationIndex;
        result = 31 * result + (lastUnlockedAt != null ? lastUnlockedAt.hashCode() : 0);
        return result;
    }

    @Override
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
