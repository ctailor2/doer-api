package com.doerapispring.domain;

import java.time.Clock;
import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class MasterList implements IMasterList, UniquelyIdentifiable<String> {
    private static final String NAME = "now";
    private static final String DEFERRED_NAME = "later";
    private static final long UNLOCK_DURATION = 1800000L;
    private static final int MAX_SIZE = 2;
    private final List<String> tasks;
    private final Clock clock;
    private final UniqueIdentifier<String> uniqueIdentifier;

    private Integer demarcationIndex = 0;
    private Date lastUnlockedAt;

    public MasterList(Clock clock, UniqueIdentifier<String> uniqueIdentifier, Date lastUnlockedAt, List<String> tasks, Integer demarcationIndex) {
        this.clock = clock;
        this.uniqueIdentifier = uniqueIdentifier;
        this.tasks = tasks;
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
        tasks.add(0, task);
        demarcationIndex++;
    }

    @Override
    public List<Todo> getTodos() {
        List<String> subList = tasks.subList(0, demarcationIndex);
        return subList.stream()
            .map(task -> new Todo(String.valueOf(tasks.indexOf(task)), task))
            .collect(toList());
    }

    @Override
    public void addDeferred(String task) throws DuplicateTodoException {
        if (alreadyExists(task)) {
            throw new DuplicateTodoException();
        }
        tasks.add(task);
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
        String task = getTaskByLocalIdentifier(localIdentifier);
        if (tasks.indexOf(task) < demarcationIndex) {
            demarcationIndex--;
        }
        tasks.remove(task);
    }

    @Override
    public void displace(String task) throws DuplicateTodoException, ListNotFullException {
        if (!isFull()) throw new ListNotFullException();
        if (alreadyExists(task)) throw new DuplicateTodoException();
        tasks.add(0, task);
    }

    @Override
    public void update(String localIdentifier, String task) throws TodoNotFoundException, DuplicateTodoException {
        if (alreadyExists(task)) {
            throw new DuplicateTodoException();
        }
        String taskToUpdate = getTaskByLocalIdentifier(localIdentifier);
        int index = tasks.indexOf(taskToUpdate);
        tasks.remove(taskToUpdate);
        tasks.add(index, task);
    }

    @Override
    public String complete(String localIdentifier) throws TodoNotFoundException {
        String task = getTaskByLocalIdentifier(localIdentifier);
        delete(localIdentifier);
        return task;
    }

    @Override
    public void move(String localIdentifier, String targetLocalIdentifier) throws TodoNotFoundException {
        String taskToMove = getTaskByLocalIdentifier(localIdentifier);
        int index = tasks.indexOf(taskToMove);
        String targetTask = getTaskByLocalIdentifier(targetLocalIdentifier);
        int targetIndex = tasks.indexOf(targetTask);

        String task = tasks.remove(index);
        tasks.add(targetIndex, task);
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
        while (demarcationIndex < tasks.size() && getTodos().size() < MAX_SIZE) {
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

    private String getTaskByLocalIdentifier(String localIdentifier) throws TodoNotFoundException {
        try {
            return tasks.get(Integer.valueOf(localIdentifier));
        } catch (IndexOutOfBoundsException e) {
            throw new TodoNotFoundException();
        }
    }

    private boolean alreadyExists(String task) {
        return tasks.contains(task);
    }

    private List<Todo> deferredTodos() {
        List<String> subList = tasks.subList(demarcationIndex, tasks.size());
        return subList.stream()
            .map(task -> new Todo(String.valueOf(tasks.indexOf(task)), task))
            .collect(toList());
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
        return tasks.stream()
            .map(task -> new Todo(String.valueOf(tasks.indexOf(task)), task))
            .collect(toList());
    }

    public Date getLastUnlockedAt() {
        return lastUnlockedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MasterList that = (MasterList) o;

        if (tasks != null ? !tasks.equals(that.tasks) : that.tasks != null) return false;
        if (clock != null ? !clock.equals(that.clock) : that.clock != null) return false;
        if (uniqueIdentifier != null ? !uniqueIdentifier.equals(that.uniqueIdentifier) : that.uniqueIdentifier != null)
            return false;
        if (demarcationIndex != null ? !demarcationIndex.equals(that.demarcationIndex) : that.demarcationIndex != null)
            return false;
        return lastUnlockedAt != null ? lastUnlockedAt.equals(that.lastUnlockedAt) : that.lastUnlockedAt == null;
    }

    @Override
    public int hashCode() {
        int result = tasks != null ? tasks.hashCode() : 0;
        result = 31 * result + (clock != null ? clock.hashCode() : 0);
        result = 31 * result + (uniqueIdentifier != null ? uniqueIdentifier.hashCode() : 0);
        result = 31 * result + (demarcationIndex != null ? demarcationIndex.hashCode() : 0);
        result = 31 * result + (lastUnlockedAt != null ? lastUnlockedAt.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MasterList{" +
            "tasks=" + tasks +
            ", clock=" + clock +
            ", uniqueIdentifier=" + uniqueIdentifier +
            ", demarcationIndex=" + demarcationIndex +
            ", lastUnlockedAt=" + lastUnlockedAt +
            '}';
    }
}
