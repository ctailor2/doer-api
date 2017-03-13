package com.doerapispring.domain;

import java.util.List;

public interface AggregateRootRepository<Parent extends UniquelyIdentifiable, Child, U> extends ObjectRepository<Parent, U> {
    default void add(Parent parent, Child child) throws AbnormalModelException {}

    default void remove(Parent parent, Child child) throws AbnormalModelException {}

    default void update(Parent parent, Child child) throws AbnormalModelException {}

    default void update(Parent parent, List<Child> children) throws AbnormalModelException {}
}
