package com.doerapispring.web;

import org.springframework.hateoas.Link;

interface HateoasLinkGenerator {
    Link signupLink();

    Link loginLink();

    Link baseResourcesLink();

    Link rootResourcesLink();

    Link historyResourcesLink();

    Link todoResourcesLink();

    Link todosLink(String scheduling);

    Link deleteTodoLink(String localId);

    Link displaceTodoLink(String localId);

    Link updateTodoLink(String localId);

    Link completeTodoLink(String localId);

    Link completedTodosLink();

    Link moveTodoLink(String localId, String targetLocalId);

    Link listPullTodosLink();

    Link listUnlockLink();

    Link listLink();

    Link todosLink();

    Link createTodoLink();

    Link createDeferredTodoLink();

    Link deferredTodosLink();
}
