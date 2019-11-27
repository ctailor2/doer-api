package com.doerapispring.web;

import com.doerapispring.authentication.AccessDeniedException;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class HateoasLinkGeneratorImpl implements HateoasLinkGenerator {
    @Override
    public Link listResourcesLink() {
        return linkTo(methodOn(ResourcesController.class).list(null)).withSelfRel();
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
        return linkTo(methodOn(TodosController.class).displace(null, listId, null)).withSelfRel();
    }

    @Override
    public Link updateTodoLink(String listId, String todoId) {
        return linkTo(methodOn(TodosController.class).update(null, listId, todoId, null)).withSelfRel();
    }

    @Override
    public Link completeTodoLink(String listId, String todoId) {
        return linkTo(methodOn(TodosController.class).complete(null, listId, todoId)).withSelfRel();
    }

    @Override
    public Link moveTodoLink(String listId, String todoId, String targetTodoId) {
        return linkTo(methodOn(TodosController.class).move(null, listId, todoId, targetTodoId)).withSelfRel();
    }

    @Override
    public Link listPullTodosLink(String listId) {
        return linkTo(methodOn(TodosController.class).pull(null, listId)).withSelfRel();
    }

    @Override
    public Link rootResourcesLink() {
        return linkTo(methodOn(ResourcesController.class).root()).withSelfRel();
    }

    @Override
    public Link historyResourcesLink() {
        return linkTo(methodOn(ResourcesController.class).history(null)).withSelfRel();
    }

    @Override
    public Link listUnlockLink(String listId) {
        return linkTo(methodOn(ListsController.class).unlock(null, listId)).withSelfRel();
    }

    @Override
    public Link listLink(String listId) {
        return linkTo(methodOn(ListsController.class).show(null, listId)).withSelfRel();
    }

    @Override
    public Link createTodoLink(String listId) {
        return linkTo(methodOn(TodosController.class).create(null, listId, null)).withSelfRel();
    }

    @Override
    public Link createDeferredTodoLink(String listId) {
        return linkTo(methodOn(TodosController.class).createDeferred(null, listId, null)).withSelfRel();
    }

    @Override
    public Link completedListLink(String listId) {
        return linkTo(methodOn(ListsController.class).showCompleted(null, listId)).withSelfRel();
    }

    @Override
    public Link listEscalateTodoLink(String listId) {
        return linkTo(methodOn(TodosController.class).escalate(null, listId)).withSelfRel();
    }

    @Override
    public Link showListsLink() {
        return linkTo(methodOn(ListsController.class).showAll(null)).withSelfRel();
    }

    @Override
    public Link createListsLink() {
        return linkTo(methodOn(ListsController.class).create(null, null)).withSelfRel();
    }

    @Override
    public Link defaultListLink() {
        return linkTo(methodOn(ListsController.class).showDefault(null)).withSelfRel();
    }
}
