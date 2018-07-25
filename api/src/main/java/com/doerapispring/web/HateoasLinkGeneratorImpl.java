package com.doerapispring.web;

import com.doerapispring.authentication.AccessDeniedException;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class HateoasLinkGeneratorImpl implements HateoasLinkGenerator {
    @Override
    public Link todoResourcesLink() {
        return linkTo(methodOn(ResourcesController.class).todo(null)).withSelfRel();
    }

    @Override
    public Link signupLink() {
        try {
            return linkTo(methodOn(UserSessionsController.class).signup(null)).withSelfRel();
        } catch (AccessDeniedException e) {
            throw new RuntimeException("Failed to create link", e);
        }
    }

    @Override
    public Link loginLink() {
        try {
            return linkTo(methodOn(UserSessionsController.class).login(null)).withSelfRel();
        } catch (AccessDeniedException e) {
            throw new RuntimeException("Failed to create link", e);
        }
    }

    @Override
    public Link baseResourcesLink() {
        return linkTo(methodOn(ResourcesController.class).base()).withSelfRel();
    }

    @Override
    public Link deleteTodoLink(String localId) {
        return linkTo(methodOn(TodosController.class).delete(null, localId)).withSelfRel();
    }

    @Override
    public Link displaceTodoLink() {
        try {
            return linkTo(methodOn(TodosController.class).displace(null, null)).withSelfRel();
        } catch (InvalidRequestException e) {
            throw new RuntimeException("Failed to create link", e);
        }
    }

    @Override
    public Link updateTodoLink(String localId) {
        try {
            return linkTo(methodOn(TodosController.class).update(null, localId, null)).withSelfRel();
        } catch (InvalidRequestException e) {
            throw new RuntimeException("Failed to create link", e);
        }
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
    public Link listPullTodosLink() {
        return linkTo(methodOn(TodosController.class).pull(null)).withSelfRel();
    }

    @Override
    public Link rootResourcesLink() {
        return linkTo(methodOn(ResourcesController.class).root()).withSelfRel();
    }

    @Override
    public Link historyResourcesLink() {
        return linkTo(methodOn(ResourcesController.class).history()).withSelfRel();
    }

    @Override
    public Link listUnlockLink() {
        return linkTo(methodOn(ListsController.class).unlock(null)).withSelfRel();
    }

    @Override
    public Link listLink() {
        return linkTo(methodOn(ListsController.class).show(null)).withSelfRel();
    }

    @Override
    public Link createTodoLink() {
        try {
            return linkTo(methodOn(TodosController.class).create(null, null)).withSelfRel();
        } catch (InvalidRequestException e) {
            throw new RuntimeException("Failed to create link", e);
        }
    }

    @Override
    public Link createDeferredTodoLink() {
        try {
            return linkTo(methodOn(TodosController.class).createDeferred(null, null)).withSelfRel();
        } catch (InvalidRequestException e) {
            throw new RuntimeException("Failed to create link", e);
        }
    }

    @Override
    public Link deferredTodosLink() {
        return linkTo(methodOn(TodosController.class).deferredTodos(null)).withSelfRel();
    }

    @Override
    public Link completedListLink() {
        return linkTo(methodOn(ListsController.class).showCompleted(null)).withSelfRel();
    }

    @Override
    public Link todosLink() {
        return linkTo(methodOn(TodosController.class).todos(null)).withSelfRel();
    }
}
