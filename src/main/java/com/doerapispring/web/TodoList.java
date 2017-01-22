package com.doerapispring.web;

import java.util.List;

public class TodoList {
    private final List<TodoDTO> todoDTOs;
    private final boolean schedulingForNowAllowed;

    public TodoList(List<TodoDTO> todoDTOs, boolean schedulingForNowAllowed) {
        this.todoDTOs = todoDTOs;
        this.schedulingForNowAllowed = schedulingForNowAllowed;
    }

    public List<TodoDTO> getTodoDTOs() {
        return todoDTOs;
    }

    public boolean isSchedulingForNowAllowed() {
        return schedulingForNowAllowed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TodoList that = (TodoList) o;

        if (schedulingForNowAllowed != that.schedulingForNowAllowed) return false;
        return todoDTOs != null ? todoDTOs.equals(that.todoDTOs) : that.todoDTOs == null;

    }

    @Override
    public int hashCode() {
        int result = todoDTOs != null ? todoDTOs.hashCode() : 0;
        result = 31 * result + (schedulingForNowAllowed ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TodoList{" +
                "todoDTOs=" + todoDTOs +
                ", schedulingForNowAllowed=" + schedulingForNowAllowed +
                '}';
    }
}
