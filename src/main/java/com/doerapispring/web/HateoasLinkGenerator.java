package com.doerapispring.web;

import org.springframework.hateoas.Link;

interface HateoasLinkGenerator {
    Link homeLink();

    Link todosLink();

    Link signupLink();

    Link loginLink();

    Link baseResourcesLink();

    Link createTodoForNowLink();

    Link createTodoForLaterLink();

    Link deleteTodoLink(Integer localId);
}
