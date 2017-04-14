package com.doerapispring.web;

import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class HateoasLinkGeneratorImpl implements HateoasLinkGenerator {
    @Override
    public Link homeLink() {
        return linkTo(methodOn(HomeController.class).home(null)).withSelfRel();
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

    @Override
    public Link deleteTodoLink(String localId) {
        return linkTo(methodOn(TodosController.class).delete(null, localId)).withSelfRel();
    }

    @Override
    public Link displaceTodoLink(String localId) {
        return linkTo(methodOn(TodosController.class).displace(null, localId, null)).withSelfRel();
    }

    @Override
    public Link updateTodoLink(String localId) {
        return linkTo(methodOn(TodosController.class).update(null, localId, null)).withSelfRel();
    }

    @Override
    public Link completeTodoLink(String localId) {
        return linkTo(methodOn(TodosController.class).complete(null, localId)).withSelfRel();
    }

    @Override
    public Link completedTodosLink() {
        return linkTo(methodOn(TodosController.class).completedTodos(null)).withSelfRel();
    }

    @Override
    public Link moveTodoLink(String localId, String targetLocalId) {
        return linkTo(methodOn(TodosController.class).move(null, localId, targetLocalId)).withSelfRel();
    }

    @Override
    public Link pullTodosLink() {
        return linkTo(methodOn(TodosController.class).pull(null)).withSelfRel();
    }
}
