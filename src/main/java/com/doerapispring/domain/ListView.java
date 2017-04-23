package com.doerapispring.domain;

import java.util.Date;

public class ListView {
    private Date lastViewedAt;

    public ListView() {
        this.lastViewedAt = new Date();
    }

    public ListView(Date lastViewedAt) {
        this.lastViewedAt = lastViewedAt;
    }

    Date getLastViewedAt() {
        return lastViewedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ListView listView = (ListView) o;

        return lastViewedAt != null ? lastViewedAt.equals(listView.lastViewedAt) : listView.lastViewedAt == null;

    }

    @Override
    public int hashCode() {
        return lastViewedAt != null ? lastViewedAt.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ListView{" +
                "lastViewedAt=" + lastViewedAt +
                '}';
    }
}
