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

    Link deleteTodoLink(String localId);

    Link displaceTodoLink(String localId);

    Link updateTodoLink(String localId);

    Link completeTodoLink(String localId);

    Link completedTodosLink();

    Link moveTodoLink(String localId, String targetLocalId);

    Link pullTodosLink();
}
