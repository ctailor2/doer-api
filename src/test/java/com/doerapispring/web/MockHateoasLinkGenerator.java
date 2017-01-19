package com.doerapispring.web;

import org.springframework.hateoas.Link;

public class MockHateoasLinkGenerator implements HateoasLinkGenerator {
    public static final String MOCK_BASE_URL = "http://some.api";

    @Override
    public Link homeLink() {
        return new Link(MOCK_BASE_URL + "/home");
    }

    @Override
    public Link todosLink(String scheduling) {
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
}
