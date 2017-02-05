package com.doerapispring.web;

import org.springframework.hateoas.Link;

public interface HateoasLinkGenerator {
    Link homeLink();

    Link todosLink();

    Link signupLink();

    Link loginLink();

    Link baseResourcesLink();

    Link createTodoForNowLink();

    Link createTodoForLaterLink();

    Link deleteTodoLink(String localId);

    Link displaceTodoLink(String localId);
}
