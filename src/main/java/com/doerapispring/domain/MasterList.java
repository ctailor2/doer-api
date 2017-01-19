package com.doerapispring.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MasterList implements UniquelyIdentifiable<String> {
    private final UniqueIdentifier<String> uniqueIdentifier;
    private final ImmediateList immediateList;
    private final PostponedList postponedList;

    MasterList(UniqueIdentifier uniqueIdentifier,
               ImmediateList immediateList,
               PostponedList postponedList) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.immediateList = immediateList;
        this.postponedList = postponedList;
    }

    static public MasterList newEmpty(UniqueIdentifier uniqueIdentifier) {
        return new MasterList(uniqueIdentifier,
                new ImmediateList(new ArrayList<>()),
                new PostponedList(new ArrayList<>()));
    }

    public ImmediateList getImmediateList() {
        return immediateList;
    }

    public PostponedList getPostponedList() {
        return postponedList;
    }

    public List<Todo> getAllTodos() {
        ArrayList<Todo> allTodos = new ArrayList<>(getImmediateList().getTodos());
        allTodos.addAll(getPostponedList().getTodos());
        return allTodos;
    }

    @Override
    public UniqueIdentifier<String> getIdentifier() {
        return uniqueIdentifier;
    }

    public Todo add(String task, ScheduledFor scheduling) {
        if (scheduling.equals(ScheduledFor.now)) {
            return immediateList.add(task);
        } else {
            return postponedList.add(task);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MasterList that = (MasterList) o;

        if (uniqueIdentifier != null ? !uniqueIdentifier.equals(that.uniqueIdentifier) : that.uniqueIdentifier != null)
            return false;
        if (immediateList != null ? !immediateList.equals(that.immediateList) : that.immediateList != null)
            return false;
        return postponedList != null ? postponedList.equals(that.postponedList) : that.postponedList == null;

    }

    @Override
    public int hashCode() {
        int result = uniqueIdentifier != null ? uniqueIdentifier.hashCode() : 0;
        result = 31 * result + (immediateList != null ? immediateList.hashCode() : 0);
        result = 31 * result + (postponedList != null ? postponedList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MasterList{" +
                "userIdentifier=" + uniqueIdentifier +
                ", immediateList=" + immediateList +
                ", postponedList=" + postponedList +
                '}';
    }

    public void displace(String localIdentifier, String task) {
        Todo todo = immediateList.getTodos().stream()
                .filter(immediateTodo ->
                        Objects.equals(immediateTodo.getLocalIdentifier(), localIdentifier))
                .collect(Collectors.toList()).get(0);
    }
}
