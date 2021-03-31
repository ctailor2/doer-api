package com.doerapispring.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.RepresentationModel;

class CompletedListResponse extends RepresentationModel<CompletedListResponse> {
    @JsonProperty("list")
    private CompletedListDTO completedListDTO;

    CompletedListResponse(CompletedListDTO completedListDTO) {
        this.completedListDTO = completedListDTO;
    }

    CompletedListDTO getCompletedListDTO() {
        return completedListDTO;
    }
}
