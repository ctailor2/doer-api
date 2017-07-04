package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping(value = "/v1/resources")
class ResourcesController {
    private final HateoasLinkGenerator hateoasLinkGenerator;

    @Autowired
    ResourcesController(HateoasLinkGenerator hateoasLinkGenerator) {
        this.hateoasLinkGenerator = hateoasLinkGenerator;
    }

    @RequestMapping(value = "/base", method = RequestMethod.GET)
    @ResponseBody
    ResponseEntity<ResourcesResponse> base() {
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(hateoasLinkGenerator.baseResourcesLink().withSelfRel(),
            hateoasLinkGenerator.loginLink().withRel("login"),
            hateoasLinkGenerator.signupLink().withRel("signup"));
        return ResponseEntity.status(HttpStatus.OK).body(resourcesResponse);
    }

    @RequestMapping(value = "/root", method = RequestMethod.GET)
    @ResponseBody
    ResponseEntity<ResourcesResponse> root() {
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(hateoasLinkGenerator.rootResourcesLink().withSelfRel(),
            hateoasLinkGenerator.todoResourcesLink().withRel("todoResources"),
            hateoasLinkGenerator.historyResourcesLink().withRel("historyResources"));
        return ResponseEntity.status(HttpStatus.OK).body(resourcesResponse);
    }

    @RequestMapping(value = "/todo", method = RequestMethod.GET)
    @ResponseBody
    ResponseEntity<ResourcesResponse> todo(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(hateoasLinkGenerator.todoResourcesLink().withSelfRel());
        resourcesResponse.add(hateoasLinkGenerator.listLink().withRel("list"));
        return ResponseEntity.status(HttpStatus.OK).body(resourcesResponse);
    }

    @RequestMapping(value = "/history", method = RequestMethod.GET)
    @ResponseBody
    ResponseEntity<ResourcesResponse> history() {
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(hateoasLinkGenerator.historyResourcesLink().withSelfRel(),
            hateoasLinkGenerator.completedTodosLink().withRel("completedTodos"));
        return ResponseEntity.status(HttpStatus.OK).body(resourcesResponse);
    }
}
