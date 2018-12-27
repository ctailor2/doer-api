package com.doerapispring.web;

import org.springframework.hateoas.Link;

interface HateoasLinkGenerator {
    Link signupLink();

    Link loginLink();

    Link baseResourcesLink();

    Link rootResourcesLink();

    Link historyResourcesLink();

    Link todoResourcesLink();

    Link deleteTodoLink(String listId, String todoId);

    Link displaceTodoLink(String listId);

    Link updateTodoLink(String localId);

    Link completeTodoLink(String listId, String localId);

    Link moveTodoLink(String listId, String todoId, String targetTodoId);

    Link listPullTodosLink(String listId);

    Link listUnlockLink(String listId);

    Link listLink(String listId);

    Link createTodoLink(String listId);

    Link createDeferredTodoLink(String listId);

    Link completedListLink();

    Link listEscalateTodoLink(String listId);
}
