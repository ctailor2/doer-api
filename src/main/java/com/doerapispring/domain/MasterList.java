package com.doerapispring.domain;

import java.util.ArrayList;
import java.util.List;

public class MasterList {
    private final ImmediateList immediateList;
    private final PostponedList postponedList;

    public MasterList(ImmediateList immediateList,
                      PostponedList postponedList) {
        this.immediateList = immediateList;
        this.postponedList = postponedList;
    }

    public ImmediateList getImmediateList() {
        return immediateList;
    }

    public PostponedList getPostponedList() {
        return postponedList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MasterList that = (MasterList) o;

        if (immediateList != null ? !immediateList.equals(that.immediateList) : that.immediateList != null)
            return false;
        return postponedList != null ? postponedList.equals(that.postponedList) : that.postponedList == null;

    }

    @Override
    public int hashCode() {
        int result = immediateList != null ? immediateList.hashCode() : 0;
        result = 31 * result + (postponedList != null ? postponedList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MasterList{" +
                "immediateList=" + immediateList +
                ", postponedList=" + postponedList +
                '}';
    }

    public List<Todo> getAllTodos() {
        ArrayList<Todo> allTodos = new ArrayList<>(getImmediateList().getTodos());
        allTodos.addAll(getPostponedList().getTodos());
        return allTodos;
    }
}
