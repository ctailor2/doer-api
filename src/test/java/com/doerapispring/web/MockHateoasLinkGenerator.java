package com.doerapispring.web;

import org.springframework.hateoas.Link;

class MockHateoasLinkGenerator implements HateoasLinkGenerator {
    static final String MOCK_BASE_URL = "http://some.api";

    @Override
    public Link todoResourcesLink() {
        return new Link(MOCK_BASE_URL + "/todoResources");
    }

    @Override
    public Link todosLink(String scheduling) {
        return new Link(MOCK_BASE_URL + "/todos?scheduling=" + scheduling);
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
    public Link createTodoForNowLink() {
        return new Link(MOCK_BASE_URL + "/createTodoForNow");
    }

    @Override
    public Link createTodoForLaterLink() {
        return new Link(MOCK_BASE_URL + "/createTodoForLater");
    }

    @Override
    public Link deleteTodoLink(String localId) {
        return new Link(MOCK_BASE_URL + "/deleteTodo/" + localId);
    }

    @Override
    public Link displaceTodoLink(String localId) {
        return new Link(MOCK_BASE_URL + "/displaceTodo/" + localId);
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
    public Link pullTodosLink() {
        return new Link(MOCK_BASE_URL + "/todos/pullTodos");
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
    public Link unlockListLink() {
        return new Link(MOCK_BASE_URL + "/todos/unlockTodos");
    }
}
