package com.doerapispring.web;

import org.springframework.hateoas.ResourceSupport;

import java.util.List;

public class TodoListReadModelDTO extends ResourceSupport {
    private final String profileName;
    private final String name;
    private final String deferredName;
    private final List<TodoDTO> todos;
    private final List<TodoDTO> deferredTodos;
    private final Long unlockDuration;

    TodoListReadModelDTO(
        String profileName,
        String name,
        String deferredName,
        List<TodoDTO> todos,
        List<TodoDTO> deferredTodos,
        Long unlockDuration
    ) {
        this.profileName = profileName;
        this.name = name;
        this.deferredName = deferredName;
        this.todos = todos;
        this.deferredTodos = deferredTodos;
        this.unlockDuration = unlockDuration;
    }

    @SuppressWarnings("WeakerAccess") // needs to be public to get serialized
    public String getProfileName() {
        return profileName;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("WeakerAccess") // needs to be public to get serialized
    public String getDeferredName() {
        return deferredName;
    }

    public List<TodoDTO> getTodos() {
        return todos;
    }

    public List<TodoDTO> getDeferredTodos() {
        return deferredTodos;
    }

    @SuppressWarnings("WeakerAccess") // needs to be public to get serialized
    public Long getUnlockDuration() {
        return unlockDuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TodoListReadModelDTO that = (TodoListReadModelDTO) o;

        if (profileName != null ? !profileName.equals(that.profileName) : that.profileName != null) return false;
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
        result = 31 * result + (profileName != null ? profileName.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (deferredName != null ? deferredName.hashCode() : 0);
        result = 31 * result + (todos != null ? todos.hashCode() : 0);
        result = 31 * result + (deferredTodos != null ? deferredTodos.hashCode() : 0);
        result = 31 * result + (unlockDuration != null ? unlockDuration.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TodoListReadModelDTO{" +
            "profileName='" + profileName + '\'' +
            ", name='" + name + '\'' +
            ", deferredName='" + deferredName + '\'' +
            ", todos=" + todos +
            ", deferredTodos=" + deferredTodos +
            ", unlockDuration=" + unlockDuration +
            '}';
    }
}
