package com.doerapispring.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.ResourceSupport;

class MasterListResponse extends ResourceSupport {
    @JsonProperty("list")
    private final MasterListDTO masterListDTO;

    MasterListResponse(MasterListDTO masterListDTO) {
        this.masterListDTO = masterListDTO;
    }

    MasterListDTO getMasterListDTO() {
        return masterListDTO;
    }
}
