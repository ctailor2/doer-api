package com.doerapispring.storage;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class TodoListEventStoreEntityKey implements Serializable {
    @Column(name = "user_id")
    public String userId;

    @Column(name = "list_id")
    public String listId;

    @Column(name = "version")
    public int version;
}
