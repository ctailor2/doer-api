package com.doerapispring.domain;

public interface IdentityGeneratingRepository<Id>  {
    Id nextIdentifier();
}
