package com.doerapispring.web;

import org.springframework.hateoas.Link;

public interface HateoasLinkGenerator {
    Link homeLink();

    Link todosLink(String scheduling);

    Link signupLink();

    Link loginLink();

    Link baseResourcesLink();
}
