package com.doerapispring.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping(value = "/v1")
public class HomeController {
    private final HateoasLinkGenerator hateoasLinkGenerator;

    @Autowired
    public HomeController(HateoasLinkGenerator hateoasLinkGenerator) {
        this.hateoasLinkGenerator = hateoasLinkGenerator;
    }

    @RequestMapping(value = "/home", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<HomeResponse> home() {
        HomeResponse homeResponse = new HomeResponse();
        homeResponse.add(hateoasLinkGenerator.homeLink().withSelfRel());
        homeResponse.add(hateoasLinkGenerator.todosLink().withRel("todos"));
        homeResponse.add(hateoasLinkGenerator.completedTodosLink().withRel("completedTodos"));
        return ResponseEntity.status(HttpStatus.OK).body(homeResponse);
    }
}
