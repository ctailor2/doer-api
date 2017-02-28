package com.doerapispring.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.ResourceSupport;

import java.util.Date;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class TodoDTO extends ResourceSupport {
    private final String localIdentifier;
    private final String task;
    private final String scheduling;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private final Date completedAt;

    public TodoDTO(String localIdentifier, String task, String scheduling) {
        this.localIdentifier = localIdentifier;
        this.task = task;
        this.scheduling = scheduling;
        this.completedAt = null;
    }

    // TODO: Nulling out fields and using json include non null is not ideal
    public TodoDTO(String task, Date completedAt) {
        this.task = task;
        this.completedAt = completedAt;
        this.localIdentifier = null;
        this.scheduling = null;
    }

    @JsonProperty("id")
    String getLocalIdentifier() {
        return localIdentifier;
    }

    public String getTask() {
        return task;
    }

    public String getScheduling() {
        return scheduling;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TodoDTO todoDTO = (TodoDTO) o;

        if (localIdentifier != null ? !localIdentifier.equals(todoDTO.localIdentifier) : todoDTO.localIdentifier != null) return false;
        if (task != null ? !task.equals(todoDTO.task) : todoDTO.task != null) return false;
        return scheduling != null ? scheduling.equals(todoDTO.scheduling) : todoDTO.scheduling == null;

    }

    @Override
    public int hashCode() {
        int result = localIdentifier != null ? localIdentifier.hashCode() : 0;
        result = 31 * result + (task != null ? task.hashCode() : 0);
        result = 31 * result + (scheduling != null ? scheduling.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TodoDTO{" +
                "localIdentifier='" + localIdentifier + '\'' +
                ", task='" + task + '\'' +
                ", scheduling='" + scheduling + '\'' +
                '}';
    }
}
