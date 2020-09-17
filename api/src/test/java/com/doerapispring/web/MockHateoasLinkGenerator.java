package com.doerapispring.web;

import org.springframework.hateoas.Link;
import scala.Int;

class MockHateoasLinkGenerator implements HateoasLinkGenerator {
    static final String MOCK_BASE_URL = "http://some.api";

    @Override
    public Link listResourcesLink() {
        return new Link(MOCK_BASE_URL + "/listResources");
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
    public Link deleteTodoLink(String listId, Int index) {
        return new Link(MOCK_BASE_URL + "/lists/" + listId + "/deleteTodo/" + index);
    }

    @Override
    public Link displaceTodoLink(String listId) {
        return new Link(MOCK_BASE_URL + "/lists/" + listId + "/displaceTodo");
    }

    @Override
    public Link updateTodoLink(String listId, Int index) {
        return new Link(MOCK_BASE_URL + "/lists/" + listId + "/updateTodo/" + index);
    }

    @Override
    public Link completeTodoLink(String listId, Int index) {
        return new Link(MOCK_BASE_URL + "/lists/" + listId + "/completeTodo/" + index);
    }

    @Override
    public Link moveTodoLink(String listId, Int index, Int targetIndex) {
        return new Link(MOCK_BASE_URL + "/lists/" + listId + "/todos/" + index + "/moveTodo/" + targetIndex);
    }

    @Override
    public Link listPullTodosLink(String listId) {
        return new Link(MOCK_BASE_URL + "/lists/" + listId + "/pullTodos");
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
    public Link listUnlockLink(String listId) {
        return new Link(MOCK_BASE_URL + "/lists/" + listId + "/unlockTodos");
    }

    @Override
    public Link listLink(String listId) {
        return new Link(MOCK_BASE_URL + "/lists/" + listId);
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
    public Link completedListLink(String listId) {
        return new Link(MOCK_BASE_URL + "/lists/" + listId + "/completedList");
    }

    @Override
    public Link listEscalateTodoLink(String listId) {
        return new Link(MOCK_BASE_URL + "/lists/" + listId + "/escalateTodo");
    }

    @Override
    public Link showListsLink() {
        return new Link(MOCK_BASE_URL + "/lists");
    }

    @Override
    public Link createListsLink() {
        return new Link(MOCK_BASE_URL + "/lists");
    }

    @Override
    public Link defaultListLink() {
        return new Link(MOCK_BASE_URL + "/lists/defaultList");
    }

    @Override
    public Link setDefaultListLink(String listId) {
        return new Link(MOCK_BASE_URL + "/lists/" + listId + "/setDefaultList");
    }
}
