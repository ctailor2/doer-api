package com.doerapispring.web;

import org.springframework.hateoas.Link;

interface HateoasLinkGenerator {
    Link signupLink();

    Link loginLink();

    Link baseResourcesLink();

    Link rootResourcesLink();

    Link historyResourcesLink();

    Link todoResourcesLink();

    Link deleteTodoLink(String localId);

    Link displaceTodoLink();

    Link updateTodoLink(String localId);

    Link completeTodoLink(String localId);

    Link moveTodoLink(String localId, String targetLocalId);

    Link listPullTodosLink();

    Link listUnlockLink();

    Link listLink();

    Link listOneLink(String listId);

    Link createTodoLink(String listId);

    Link createDeferredTodoLink(String listID);

    Link completedListLink();

    Link listEscalateTodoLink();
}
