package com.doerapispring.domain;

public class ListId {
    private String name;

    public ListId(String name) {
        this.name = name;
    }

    public String get() {
        return name;
    }

//  TODO: Added just for marshaling/unmarshaling
    public ListId() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ListId listId = (ListId) o;

        return name != null ? name.equals(listId.name) : listId.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ListId{" +
            "name='" + name + '\'' +
            '}';
    }
}
