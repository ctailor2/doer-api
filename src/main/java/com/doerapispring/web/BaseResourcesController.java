package com.doerapispring.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping(value = "/v1")
public class BaseResourcesController {
    private final HateoasLinkGenerator hateoasLinkGenerator;

    @Autowired
    public BaseResourcesController(HateoasLinkGenerator hateoasLinkGenerator) {
        this.hateoasLinkGenerator = hateoasLinkGenerator;
    }

    @RequestMapping(value = "/baseResources", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<BaseResourcesResponse> baseResources() {
        BaseResourcesResponse baseResourcesResponse = new BaseResourcesResponse();
        baseResourcesResponse.add(hateoasLinkGenerator.baseResourcesLink().withSelfRel(),
                hateoasLinkGenerator.loginLink().withRel("login"),
                hateoasLinkGenerator.signupLink().withRel("signup"));
        return ResponseEntity.status(HttpStatus.OK).body(baseResourcesResponse);
    }
}
