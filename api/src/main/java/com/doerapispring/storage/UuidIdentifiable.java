package com.doerapispring.storage;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

abstract class UuidIdentifiable {
    public String uuid;

    @Id
    @Column(value = "uuid")
    public String id;
}
