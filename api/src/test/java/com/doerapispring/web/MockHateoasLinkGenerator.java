package com.doerapispring.web;

import org.springframework.hateoas.Link;

class MockHateoasLinkGenerator implements HateoasLinkGenerator {
    static final String MOCK_BASE_URL = "http://some.api";

    @Override
    public Link todoResourcesLink() {
        return new Link(MOCK_BASE_URL + "/todoResources");
    }

    @Override
    public Link signupLink() {
        return new Link(MOCK_BASE_URL + "/signup");
    }

    @Override
    public Link loginLink() {
        return new Link(MOCK_BASE_URL + "/login");
    }

    @Override
    public Link baseResourcesLink() {
        return new Link(MOCK_BASE_URL + "/baseResources");
    }

    @Override
    public Link deleteTodoLink(String localId) {
        return new Link(MOCK_BASE_URL + "/deleteTodo/" + localId);
    }

    @Override
    public Link displaceTodoLink() {
        return new Link(MOCK_BASE_URL + "/list/displaceTodo");
    }

    @Override
    public Link updateTodoLink(String localId) {
        return new Link(MOCK_BASE_URL + "/updateTodo/" + localId);
    }

    @Override
    public Link completeTodoLink(String listId, String localId) {
        return new Link(MOCK_BASE_URL + "/lists/" + listId + "/completeTodo/" + localId);
    }

    @Override
    public Link moveTodoLink(String localId, String targetLocalId) {
        return new Link(MOCK_BASE_URL + "/todos/" + localId + "/moveTodo/" + targetLocalId);
    }

    @Override
    public Link listPullTodosLink() {
        return new Link(MOCK_BASE_URL + "/list/pullTodos");
    }

    @Override
    public Link rootResourcesLink() {
        return new Link(MOCK_BASE_URL + "/rootResources");
    }

    @Override
    public Link historyResourcesLink() {
        return new Link(MOCK_BASE_URL + "/historyResources");
    }

    @Override
    public Link listUnlockLink() {
        return new Link(MOCK_BASE_URL + "/list/unlockTodos");
    }

    @Override
    public Link listLink(String listId) {
        if (listId == null) {
            return new Link(MOCK_BASE_URL + "/list");
        } else {
            return new Link(MOCK_BASE_URL + "/lists/" + listId);
        }
    }

    @Override
    public Link createTodoLink(String listId) {
        return new Link(MOCK_BASE_URL + "/lists/" + listId + "/createTodo");
    }

    @Override
    public Link createDeferredTodoLink(String listId) {
        return new Link(MOCK_BASE_URL + "/lists/" + listId + "/createDeferredTodo");
    }

    @Override
    public Link completedListLink() {
        return new Link(MOCK_BASE_URL + "/completedList");
    }

    @Override
    public Link listEscalateTodoLink() {
        return new Link(MOCK_BASE_URL + "/list/escalateTodo");
    }
}
