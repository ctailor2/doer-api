package com.doerapispring.web;

import org.springframework.hateoas.ResourceSupport;

public class MasterListDTO extends ResourceSupport {
    private final String name;
    private final String deferredName;
    private final boolean full;

    public MasterListDTO(String name, String deferredName, boolean full) {
        this.name = name;
        this.deferredName = deferredName;
        this.full = full;
    }

    public boolean isFull() {
        return full;
    }

    public String getName() {
        return name;
    }

    public String getDeferredName() {
        return deferredName;
    }
}
