package com.doerapispring.domain;

public class ListOverview {
    private final ListId listId;
    private final String name;

    public ListOverview(ListId listId, String name) {
        this.listId = listId;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ListId getListId() {
        return listId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ListOverview that = (ListOverview) o;

        if (listId != null ? !listId.equals(that.listId) : that.listId != null) return false;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        int result = listId != null ? listId.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ListOverview{" +
            "listId=" + listId +
            ", name='" + name + '\'' +
            '}';
    }
}
