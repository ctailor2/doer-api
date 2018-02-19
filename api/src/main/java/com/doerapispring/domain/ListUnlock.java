package com.doerapispring.domain;

import java.util.Date;

public class ListUnlock {
    private Date createdAt;

    public ListUnlock() {
        this.createdAt = new Date();
    }

    public ListUnlock(Date createdAt) {
        this.createdAt = createdAt;
    }

    Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ListUnlock listUnlock = (ListUnlock) o;

        return createdAt != null ? createdAt.equals(listUnlock.createdAt) : listUnlock.createdAt == null;

    }

    @Override
    public int hashCode() {
        return createdAt != null ? createdAt.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ListUnlock{" +
                "createdAt=" + createdAt +
                '}';
    }
}
