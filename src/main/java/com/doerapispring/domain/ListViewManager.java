package com.doerapispring.domain;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class ListViewManager implements UniquelyIdentifiable<String> {
    private final UniqueIdentifier<String> uniqueIdentifier;
    private final List<ListView> listViews;

    public ListViewManager(UniqueIdentifier<String> uniqueIdentifier,
                           List<ListView> listViews) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.listViews = listViews;
    }

    @Override
    public UniqueIdentifier<String> getIdentifier() {
        return uniqueIdentifier;
    }

    public List<ListView> getListViews() {
        return listViews;
    }

    ListView recordView() throws LockTimerNotExpiredException {
        Boolean viewNotAllowed = getLastViewedAt()
                .map(lastViewedAt -> lastViewedAt.after(beginningOfToday()))
                .orElse(false);
        if (viewNotAllowed) {
            throw new LockTimerNotExpiredException();
        }
        return new ListView();
    }

    private Optional<Date> getLastViewedAt() {
        return listViews.stream()
                .findFirst()
                .map(ListView::getLastViewedAt);
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

        ListViewManager that = (ListViewManager) o;

        if (uniqueIdentifier != null ? !uniqueIdentifier.equals(that.uniqueIdentifier) : that.uniqueIdentifier != null)
            return false;
        return listViews != null ? listViews.equals(that.listViews) : that.listViews == null;

    }

    @Override
    public int hashCode() {
        int result = uniqueIdentifier != null ? uniqueIdentifier.hashCode() : 0;
        result = 31 * result + (listViews != null ? listViews.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ListViewManager{" +
                "uniqueIdentifier=" + uniqueIdentifier +
                ", listViews=" + listViews +
                '}';
    }
}
