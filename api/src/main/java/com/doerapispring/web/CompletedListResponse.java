package com.doerapispring.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.ResourceSupport;

class CompletedListResponse extends ResourceSupport {
    @JsonProperty("list")
    private CompletedListDTO completedListDTO;

    CompletedListResponse(CompletedListDTO completedListDTO) {
        this.completedListDTO = completedListDTO;
    }

    CompletedListDTO getCompletedListDTO() {
        return completedListDTO;
    }
}
