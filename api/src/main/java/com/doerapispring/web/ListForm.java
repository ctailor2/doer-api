package com.doerapispring.web;

import com.fasterxml.jackson.annotation.JsonProperty;

class ListForm {
    private final String name;

    ListForm(@JsonProperty("name") String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
