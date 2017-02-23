package com.doerapispring.domain;

public interface AggregateRootRepository<Parent extends UniquelyIdentifiable, Child, U> extends ObjectRepository<Parent, U> {
    void add(Parent parent, Child child) throws AbnormalModelException;

    void remove(Parent parent, Child child) throws AbnormalModelException;

    void update(Parent parent, Child child) throws AbnormalModelException;
}
