package com.doerapispring.domain;

import java.time.Clock;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static java.util.Collections.emptyList;

public class ReadOnlyMasterList implements UniquelyIdentifiable<String> {
    private static final String NAME = "now";
    private static final String DEFERRED_NAME = "later";
    private static final long UNLOCK_DURATION = 1800000L;
    private static final int MAX_SIZE = 2;
    protected final Clock clock;
    protected final UniqueIdentifier<String> uniqueIdentifier;
    protected final List<Todo> todos;
    protected Date lastUnlockedAt;
    protected Integer demarcationIndex;

    public ReadOnlyMasterList(Clock clock,
                              UniqueIdentifier<String> uniqueIdentifier,
                              Date lastUnlockedAt,
                              List<Todo> todos,
                              Integer demarcationIndex) {
        this.clock = clock;
        this.uniqueIdentifier = uniqueIdentifier;
        this.lastUnlockedAt = lastUnlockedAt;
        this.demarcationIndex = demarcationIndex;
        this.todos = todos;
    }

    public String getName() {
        return NAME;
    }

    public String getDeferredName() {
        return DEFERRED_NAME;
    }

    public List<Todo> getTodos() {
        return todos.subList(0, demarcationIndex);
    }

    public List<Todo> getDeferredTodos() {
        if (isLocked()) {
            return emptyList();
        }
        return deferredTodos();
    }

    public Long unlockDuration() {
        long duration = lastUnlockedAt.toInstant().toEpochMilli() + UNLOCK_DURATION - clock.instant().toEpochMilli();
        if (duration > 0) {
            return duration;
        } else {
            return 0L;
        }
    }

    public boolean isFull() {
        return todos.subList(0, demarcationIndex).size() >= MAX_SIZE;
    }

    public boolean isAbleToBeUnlocked() {
        Date now = Date.from(clock.instant());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone(clock.getZone()));
        calendar.setTime(now);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date beginningOfToday = calendar.getTime();
        return isLocked() && lastUnlockedAt.before(beginningOfToday);
    }

    public boolean isAbleToBeReplenished() {
        return !isFull() && deferredTodos().size() > 0;
    }

    private boolean isLocked() {
        return lastUnlockedAt.before(Date.from(clock.instant().minusSeconds(1800L)));
    }

    private List<Todo> deferredTodos() {
        return todos.subList(demarcationIndex, todos.size());
    }

    @Override
    public UniqueIdentifier<String> getIdentifier() {
        return uniqueIdentifier;
    }
}
