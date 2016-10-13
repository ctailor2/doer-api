package com.doerapispring.todos;

/**
 * Created by chiragtailor on 10/12/16.
 */
public enum TodoTypeQueryEnum {
    active(true), inactive(false);

    private boolean value;

    public boolean getValue() {
        return value;
    }

    TodoTypeQueryEnum(boolean value) {
        this.value = value;
    }
}
