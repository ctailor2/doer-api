package com.doerapispring.domain

trait OwnedObjectReadAllRepository[T, OwnerId, Id] {
    def findAll(ownerId: OwnerId): List[T]
}
