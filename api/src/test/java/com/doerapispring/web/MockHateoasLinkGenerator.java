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
    public Link completeTodoLink(String localId) {
        return new Link(MOCK_BASE_URL + "/completeTodo/" + localId);
    }

    @Override
    public Link completedTodosLink() {
        return new Link(MOCK_BASE_URL + "/completedTodos");
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
    public Link listLink() {
        return new Link(MOCK_BASE_URL + "/list");
    }

    @Override
    public Link createTodoLink() {
        return new Link(MOCK_BASE_URL + "/list/createTodo");
    }

    @Override
    public Link createDeferredTodoLink() {
        return new Link(MOCK_BASE_URL + "/list/createDeferredTodo");
    }

    @Override
    public Link deferredTodosLink() {
        return new Link(MOCK_BASE_URL + "/list/deferredTodos");
    }

    @Override
    public Link todosLink() {
        return new Link(MOCK_BASE_URL + "/list/todos");
    }
}
