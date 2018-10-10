package com.doerapispring.web;

import org.springframework.hateoas.ResourceSupport;

import java.util.List;

public class MasterListDTO extends ResourceSupport {
    private final String name;
    private final String deferredName;
    private final List<TodoDTO> todos;
    private final List<TodoDTO> deferredTodos;
    private final Long unlockDuration;

    MasterListDTO(
        String name,
        String deferredName,
        List<TodoDTO> todos,
        List<TodoDTO> deferredTodos,
        Long unlockDuration
    ) {
        this.name = name;
        this.deferredName = deferredName;
        this.todos = todos;
        this.deferredTodos = deferredTodos;
        this.unlockDuration = unlockDuration;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        MasterListDTO that = (MasterListDTO) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (deferredName != null ? !deferredName.equals(that.deferredName) : that.deferredName != null) return false;
        if (todos != null ? !todos.equals(that.todos) : that.todos != null) return false;
        if (deferredTodos != null ? !deferredTodos.equals(that.deferredTodos) : that.deferredTodos != null)
            return false;
        return unlockDuration != null ? unlockDuration.equals(that.unlockDuration) : that.unlockDuration == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (deferredName != null ? deferredName.hashCode() : 0);
        result = 31 * result + (todos != null ? todos.hashCode() : 0);
        result = 31 * result + (deferredTodos != null ? deferredTodos.hashCode() : 0);
        result = 31 * result + (unlockDuration != null ? unlockDuration.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MasterListDTO{" +
            "name='" + name + '\'' +
            ", deferredName='" + deferredName + '\'' +
            ", todos=" + todos +
            ", deferredTodos=" + deferredTodos +
            ", unlockDuration=" + unlockDuration +
            '}';
    }
}
