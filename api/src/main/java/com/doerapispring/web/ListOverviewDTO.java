package com.doerapispring.web;

import org.springframework.hateoas.ResourceSupport;

class ListOverviewDTO extends ResourceSupport {
    private String name;

    ListOverviewDTO(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
