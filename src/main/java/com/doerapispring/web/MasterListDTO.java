package com.doerapispring.web;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.hateoas.ResourceSupport;

public class MasterListDTO extends ResourceSupport {
    private final String name;
    private final String deferredName;
    private final boolean full;
    private final boolean locked;
    private final boolean ableToBeReplenished;

    public MasterListDTO(String name, String deferredName, boolean full, boolean locked, boolean ableToBeReplenished) {
        this.name = name;
        this.deferredName = deferredName;
        this.full = full;
        this.locked = locked;
        this.ableToBeReplenished = ableToBeReplenished;
    }

    public String getName() {
        return name;
    }

    public String getDeferredName() {
        return deferredName;
    }

    @JsonIgnore
    public boolean isFull() {
        return full;
    }

    @JsonIgnore
    public boolean isLocked() {
        return locked;
    }

    public boolean isAbleToBeReplenished() {
        return ableToBeReplenished;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        MasterListDTO that = (MasterListDTO) o;

        if (full != that.full) return false;
        if (locked != that.locked) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return deferredName != null ? deferredName.equals(that.deferredName) : that.deferredName == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (deferredName != null ? deferredName.hashCode() : 0);
        result = 31 * result + (full ? 1 : 0);
        result = 31 * result + (locked ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MasterListDTO{" +
            "name='" + name + '\'' +
            ", deferredName='" + deferredName + '\'' +
            ", full=" + full +
            ", locked=" + locked +
            '}';
    }
}
