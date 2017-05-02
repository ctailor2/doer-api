package com.doerapispring.domain;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class ListManager implements UniquelyIdentifiable<String> {
    private final UniqueIdentifier<String> uniqueIdentifier;
    private final List<ListUnlock> listUnlocks;

    public ListManager(UniqueIdentifier<String> uniqueIdentifier,
                       List<ListUnlock> listUnlocks) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.listUnlocks = listUnlocks;
    }

    @Override
    public UniqueIdentifier<String> getIdentifier() {
        return uniqueIdentifier;
    }

    public List<ListUnlock> getListUnlocks() {
        return listUnlocks;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ListManager that = (ListManager) o;

        if (uniqueIdentifier != null ? !uniqueIdentifier.equals(that.uniqueIdentifier) : that.uniqueIdentifier != null)
            return false;
        return listUnlocks != null ? listUnlocks.equals(that.listUnlocks) : that.listUnlocks == null;

    }

    @Override
    public int hashCode() {
        int result = uniqueIdentifier != null ? uniqueIdentifier.hashCode() : 0;
        result = 31 * result + (listUnlocks != null ? listUnlocks.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ListManager{" +
                "uniqueIdentifier=" + uniqueIdentifier +
                ", listUnlocks=" + listUnlocks +
                '}';
    }
}
