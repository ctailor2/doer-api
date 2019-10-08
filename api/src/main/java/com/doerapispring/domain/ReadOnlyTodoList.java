package com.doerapispring.domain;

import java.time.Clock;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static java.util.Collections.emptyList;

public class ReadOnlyTodoList {
    private static final String NAME = "now";
    private static final String DEFERRED_NAME = "later";
    private static final long UNLOCK_DURATION = 1800000L;
    private static final int MAX_SIZE = 2;
    private final Clock clock;
    private final List<Todo> todos;
    private final ListId listId;
    private final String profileName;
    private final Date lastUnlockedAt;
    private final Integer demarcationIndex;

    public ReadOnlyTodoList(Clock clock,
                            String profileName,
                            Date lastUnlockedAt,
                            List<Todo> todos,
                            Integer demarcationIndex,
                            ListId listId) {
        this.clock = clock;
        this.profileName = profileName;
        this.lastUnlockedAt = lastUnlockedAt;
        this.demarcationIndex = demarcationIndex;
        this.todos = todos;
        this.listId = listId;
    }

    public ListId getListId() {
        return listId;
    }

    public String getProfileName() {
        return profileName;
    }

    public String getSectionName() {
        return NAME;
    }

    public String getDeferredSectionName() {
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

    public boolean isAbleToBeEscalated() {
        return isFull() && deferredTodos().size() > 0;
    }
}
