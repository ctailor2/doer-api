package com.doerapispring.web;

import org.springframework.hateoas.ResourceSupport;

import java.util.List;

class ListOverviewsResponse extends ResourceSupport {
    private List<ListOverviewDTO> lists;

    ListOverviewsResponse(List<ListOverviewDTO> lists) {
        this.lists = lists;
    }

    public List<ListOverviewDTO> getLists() {
        return lists;
    }
}
