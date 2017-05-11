package com.doerapispring.web;

import org.springframework.hateoas.ResourceSupport;

public class ListDTO extends ResourceSupport {
    private final String name;

    public ListDTO(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ListDTO listDTO = (ListDTO) o;

        return name != null ? name.equals(listDTO.name) : listDTO.name == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ListDTO{" +
                "name='" + name + '\'' +
                '}';
    }
}
