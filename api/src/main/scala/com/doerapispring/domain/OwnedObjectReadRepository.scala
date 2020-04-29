package com.doerapispring.domain

trait OwnedObjectReadRepository[T, OwnerId, Id] {
    def find(ownerId: OwnerId, id: Id): Option[T]
}
