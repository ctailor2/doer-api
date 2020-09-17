package com.doerapispring.web;

import org.springframework.hateoas.Link;
import scala.Int;

interface HateoasLinkGenerator {
    Link signupLink();

    Link loginLink();

    Link baseResourcesLink();

    Link rootResourcesLink();

    Link historyResourcesLink();

    Link listResourcesLink();

    Link deleteTodoLink(String listId, Int index);

    Link displaceTodoLink(String listId);

    Link updateTodoLink(String listId, Int index);

    Link completeTodoLink(String listId, Int index);

    Link moveTodoLink(String listId, Int index, Int targetIndex);

    Link listPullTodosLink(String listId);

    Link listUnlockLink(String listId);

    Link listLink(String listId);

    Link createTodoLink(String listId);

    Link createDeferredTodoLink(String listId);

    Link completedListLink(String listId);

    Link listEscalateTodoLink(String listId);

    Link showListsLink();

    Link createListsLink();

    Link defaultListLink();

    Link setDefaultListLink(String listId);
}
