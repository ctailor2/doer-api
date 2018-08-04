package com.doerapispring.web;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.hateoas.ResourceSupport;

import java.util.List;

public class MasterListDTO extends ResourceSupport {
    private final String name;
    private final String deferredName;
    private final List<TodoDTO> todos;
    private final List<TodoDTO> deferredTodos;
    private final Long unlockDuration;
    private final boolean full;
    private final boolean ableToBeUnlocked;
    private final boolean ableToBeReplenished;
    private final boolean locked;

    public MasterListDTO(
        String name,
        String deferredName,
        List<TodoDTO> todos,
        List<TodoDTO> deferredTodos,
        Long unlockDuration,
        boolean full,
        boolean locked,
        boolean ableToBeUnlocked,
        boolean ableToBeReplenished
    ) {
        this.name = name;
        this.deferredName = deferredName;
        this.todos = todos;
        this.deferredTodos = deferredTodos;
        this.unlockDuration = unlockDuration;
        this.full = full;
        this.locked = locked;
        this.ableToBeUnlocked = ableToBeUnlocked;
        this.ableToBeReplenished = ableToBeReplenished;
    }

    public String getName() {
        return name;
    }

    public String getDeferredName() {
        return deferredName;
    }

    public List<TodoDTO> getTodos() {
        return todos;
    }

    public List<TodoDTO> getDeferredTodos() {
        return deferredTodos;
    }

    public Long getUnlockDuration() {
        return unlockDuration;
    }

    @JsonIgnore
    public boolean isFull() {
        return full;
    }

    @JsonIgnore
    public boolean isAbleToBeUnlocked() {
        return ableToBeUnlocked;
    }

    @JsonIgnore
    public boolean isAbleToBeReplenished() {
        return ableToBeReplenished;
    }

    @JsonIgnore
    public boolean isLocked() {
        return locked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        MasterListDTO that = (MasterListDTO) o;

        if (full != that.full) return false;
        if (ableToBeUnlocked != that.ableToBeUnlocked) return false;
        if (ableToBeReplenished != that.ableToBeReplenished) return false;
        if (locked != that.locked) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (deferredName != null ? !deferredName.equals(that.deferredName) : that.deferredName != null) return false;
        return unlockDuration != null ? unlockDuration.equals(that.unlockDuration) : that.unlockDuration == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (deferredName != null ? deferredName.hashCode() : 0);
        result = 31 * result + (unlockDuration != null ? unlockDuration.hashCode() : 0);
        result = 31 * result + (full ? 1 : 0);
        result = 31 * result + (ableToBeUnlocked ? 1 : 0);
        result = 31 * result + (ableToBeReplenished ? 1 : 0);
        result = 31 * result + (locked ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MasterListDTO{" +
            "name='" + name + '\'' +
            ", deferredName='" + deferredName + '\'' +
            ", unlockDuration=" + unlockDuration +
            ", full=" + full +
            ", ableToBeUnlocked=" + ableToBeUnlocked +
            ", ableToBeReplenished=" + ableToBeReplenished +
            ", locked=" + locked +
            '}';
    }
}
