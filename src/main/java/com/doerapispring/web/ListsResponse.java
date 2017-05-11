package com.doerapispring.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.ResourceSupport;

import java.util.List;

class ListsResponse extends ResourceSupport {
    @JsonProperty("lists")
    private final List<ListDTO> listDTOs;

    ListsResponse(List<ListDTO> listDTOs) {
        this.listDTOs = listDTOs;
    }

    List<ListDTO> getListDTOs() {
        return listDTOs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ListsResponse that = (ListsResponse) o;

        return listDTOs != null ? listDTOs.equals(that.listDTOs) : that.listDTOs == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (listDTOs != null ? listDTOs.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ListsResponse{" +
                "listDTOs=" + listDTOs +
                '}';
    }
}
