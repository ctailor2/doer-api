package com.doerapispring.web;

import org.springframework.hateoas.Link;

class MockHateoasLinkGenerator implements HateoasLinkGenerator {
    static final String MOCK_BASE_URL = "http://some.api";

    @Override
    public Link homeLink() {
        return new Link(MOCK_BASE_URL + "/home");
    }

    @Override
    public Link todosLink() {
        return new Link(MOCK_BASE_URL + "/todos");
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
}
