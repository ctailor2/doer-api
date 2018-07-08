package com.doerapispring.domain;

import java.time.Clock;
import java.util.*;
import java.util.stream.Collectors;

public class MasterList implements IMasterList, UniquelyIdentifiable<String> {
    public static final String NAME = "now";
    public static final String DEFERRED_NAME = "later";
    private List<Todo> todos = new ArrayList<>();
    private final Clock clock;
    private final UniqueIdentifier<String> uniqueIdentifier;
    private Integer demarcationIndex = 0;
    private Date lastUnlockedAt;

    public MasterList(
        Clock clock,
        UniqueIdentifier<String> uniqueIdentifier,
        Date lastUnlockedAt) {
        this.clock = clock;
        this.uniqueIdentifier = uniqueIdentifier;
        this.lastUnlockedAt = lastUnlockedAt;
    }

    public MasterList(Clock clock, UniqueIdentifier<String> uniqueIdentifier, List<Todo> todos, Date lastUnlockedAt, Integer demarcationIndex) {
        this.clock = clock;
        this.uniqueIdentifier = uniqueIdentifier;
        this.todos = todos;
        this.lastUnlockedAt = lastUnlockedAt;
        this.demarcationIndex = demarcationIndex;
    }

    @Override
    public Todo add(String task) throws ListSizeExceededException, DuplicateTodoException {
        if (alreadyExists(task)) {
            throw new DuplicateTodoException();
        }
        if (getTodos().size() >= maxSize()) {
            throw new ListSizeExceededException();
        }
        int position;
        if (todos.isEmpty()) {
            position = 1;
        } else {
            position = todos.get(0).getPosition() - 1;
        }
        Todo todo = new Todo(generateIdentifier(), task, MasterList.NAME, position);
        todos.add(0, todo);
        demarcationIndex++;
        return todo;
    }

    @Override
    public List<Todo> getTodos() {
        return todos.subList(0, demarcationIndex);
    }

    @Override
    public Todo addDeferred(String task) throws DuplicateTodoException {
        if (alreadyExists(task)) {
            throw new DuplicateTodoException();
        }
        Todo todo = new Todo(generateIdentifier(), task, MasterList.DEFERRED_NAME, getNextDeferredPosition());
        todos.add(todo);
        return todo;
    }

    @Override
    public ListUnlock unlock() throws LockTimerNotExpiredException {
        if (!isAbleToBeUnlocked()) {
            throw new LockTimerNotExpiredException();
        }
        lastUnlockedAt = Date.from(clock.instant());
        return new ListUnlock(lastUnlockedAt);
    }

    @Override
    public List<Todo> getDeferredTodos() throws LockTimerNotExpiredException {
        if (isLocked()) {
            throw new LockTimerNotExpiredException();
        }
        return deferredTodos();
    }

    @Override
    public Todo delete(String localIdentifier) throws TodoNotFoundException {
        Todo todoToDelete = getByLocalIdentifier(localIdentifier);
        if (todos.indexOf(todoToDelete) < demarcationIndex) {
            demarcationIndex--;
        }
        todos.remove(todoToDelete);
        return todoToDelete;
    }

    @Override
    public Todo displace(String localIdentifier, String task) throws TodoNotFoundException, DuplicateTodoException {
        if (alreadyExists(task)) throw new DuplicateTodoException();
//        TODO: Forget about getting this working and just remove the displace link for now
//        Bring this back once the master list model is persisted
//        and names / positions are no longer managed externally from the masterlist
        Todo existingTodo = delete(localIdentifier);
        Todo displacingTodo = new Todo(
            generateIdentifier(),
            task,
            NAME,
            existingTodo.getPosition());
        todos.add(0, displacingTodo);
        demarcationIndex++;
        Todo todo = new Todo(existingTodo.getLocalIdentifier(), existingTodo.getTask(), DEFERRED_NAME, 1321321);
        todos.add(demarcationIndex, todo);
        return displacingTodo;
    }

    @Override
    public Todo update(String localIdentifier, String task) throws TodoNotFoundException, DuplicateTodoException {
        if (alreadyExists(task)) {
            throw new DuplicateTodoException();
        }
        Todo todo = getByLocalIdentifier(localIdentifier);
        todo.setTask(task);
        return todo;
    }

    @Override
    public String complete(String localIdentifier) throws TodoNotFoundException {
        Todo todo = delete(localIdentifier);
        return todo.getTask();
    }

    @Override
    public List<Todo> move(String localIdentifier, String targetLocalIdentifier) throws TodoNotFoundException {
        Todo todo = getByLocalIdentifier(localIdentifier);
        int index = todos.indexOf(todo);
        Todo targetTodo = getByLocalIdentifier(targetLocalIdentifier);
        int originalIndex;
        int targetIndex;
        List<Todo> subList;
        if (index < demarcationIndex) {
            subList = getTodos();
        } else {
            subList = deferredTodos();
        }
        originalIndex = subList.indexOf(todo);
        targetIndex = subList.indexOf(targetTodo);

        Direction direction = Direction.valueOf(Integer.compare(targetTodo.getPosition(), todo.getPosition()));
        Map<Integer, Integer> originalMapping = new HashMap<>();
        for (int i = originalIndex; direction.targetNotExceeded(i, targetIndex); i += direction.getValue()) {
            originalMapping.put(i, subList.get(i).getPosition());
        }

        subList.remove(todo);
        subList.add(targetIndex, todo);

        return originalMapping.entrySet().stream()
            .map(entry -> {
                Todo effectedTodo = subList.get(entry.getKey());
                effectedTodo.setPosition(entry.getValue());
                return effectedTodo;
            })
            .collect(Collectors.toList());
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
            .map(listUnlock -> listUnlock.toInstant().toEpochMilli() + 1800000L - clock.instant().toEpochMilli())
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
    public List<Todo> pull() {
        List<Todo> pulledTodos = new ArrayList<>();
        while (demarcationIndex < todos.size() && getTodos().size() < maxSize()) {
            Todo pulledTodo = todos.get(demarcationIndex);
            pulledTodo.setListName(MasterList.NAME);
            pulledTodos.add(pulledTodo);
            demarcationIndex++;
        }
        return pulledTodos;
    }

    @Override
    public boolean isFull() {
        return getTodos().size() >= maxSize();
    }

    @Override
    public boolean isAbleToBeReplenished() {
        return !isFull() && deferredTodos().size() > 0;
    }

    Todo getByLocalIdentifier(String localIdentifier) throws TodoNotFoundException {
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

    private int maxSize() {
        return 2;
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

    private int getNextDeferredPosition() {
        int position;
        if (todos.isEmpty()) {
            position = 1;
        } else {
            position = todos.get(todos.size() - 1).getPosition() + 1;
        }
        return position;
    }

    private Optional<Date> mostRecentListUnlock() {
        return Optional.ofNullable(lastUnlockedAt);
    }

    @Override
    public UniqueIdentifier<String> getIdentifier() {
        return uniqueIdentifier;
    }

    public String getName() {
        return NAME;
    }

    public String getDeferredName() {
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
