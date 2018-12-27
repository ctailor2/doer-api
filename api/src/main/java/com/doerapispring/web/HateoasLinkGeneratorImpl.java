package com.doerapispring.web;

import com.doerapispring.authentication.AccessDeniedException;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.BasicLinkBuilder;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class HateoasLinkGeneratorImpl implements HateoasLinkGenerator {
    @Override
    public Link todoResourcesLink() {
        try {
            return linkTo(methodOn(ResourcesController.class).todo(null)).withSelfRel();
        } catch (InvalidRequestException e) {
            throw new RuntimeException("Failed to create link", e);
        }
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
    public Link deleteTodoLink(String listId, String todoId) {
        return linkTo(methodOn(TodosController.class).delete(null, listId, todoId)).withSelfRel();
    }

    @Override
    public Link displaceTodoLink(String listId) {
        try {
            return linkTo(methodOn(TodosController.class).displace(null, listId, null)).withSelfRel();
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
    public Link completeTodoLink(String listId, String todoId) {
        return linkTo(methodOn(TodosController.class).complete(null, listId, todoId)).withSelfRel();
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
    public Link listLink(String listId) {
        if (listId == null) {
            return BasicLinkBuilder.linkToCurrentMapping().slash("/v1/list").withSelfRel();
        }
        return linkTo(methodOn(ListsController.class).show(null, listId)).withSelfRel();
    }

    @Override
    public Link createTodoLink(String listId) {
        try {
            return linkTo(methodOn(TodosController.class).create(null, listId, null)).withSelfRel();
        } catch (InvalidRequestException e) {
            throw new RuntimeException("Failed to create link", e);
        }
    }

    @Override
    public Link createDeferredTodoLink(String listId) {
        try {
            return linkTo(methodOn(TodosController.class).createDeferred(null, listId, null)).withSelfRel();
        } catch (InvalidRequestException e) {
            throw new RuntimeException("Failed to create link", e);
        }
    }

    @Override
    public Link completedListLink() {
        return linkTo(methodOn(ListsController.class).showCompleted(null)).withSelfRel();
    }

    @Override
    public Link listEscalateTodoLink(String listId) {
        try {
            return linkTo(methodOn(TodosController.class).escalate(null, listId)).withSelfRel();
        } catch (InvalidRequestException e) {
            throw new RuntimeException("Failed to create link", e);
        }
    }
}
