package com.doerapispring.web;

import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class HateoasLinkGeneratorImpl implements HateoasLinkGenerator {
    @Override
    public Link homeLink() {
        return linkTo(methodOn(HomeController.class).home()).withSelfRel();
    }

    @Override
    public Link todosLink() {
        return linkTo(methodOn(TodosController.class).index(null)).withSelfRel();
    }

    @Override
    public Link signupLink() {
        return linkTo(methodOn(UserSessionsController.class).signup(null)).withSelfRel();
    }

    @Override
    public Link loginLink() {
        return linkTo(methodOn(UserSessionsController.class).login(null)).withSelfRel();
    }

    @Override
    public Link baseResourcesLink() {
        return linkTo(methodOn(BaseResourcesController.class).baseResources()).withSelfRel();
    }

    @Override
    public Link createTodoForNowLink() {
        return linkTo(methodOn(TodosController.class).createForNow(null, null)).withSelfRel();
    }

    @Override
    public Link createTodoForLaterLink() {
        return linkTo(methodOn(TodosController.class).createForLater(null, null)).withSelfRel();
    }
}
