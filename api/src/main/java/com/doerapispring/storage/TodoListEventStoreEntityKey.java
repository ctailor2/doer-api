package com.doerapispring.storage;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class TodoListEventStoreEntityKey implements Serializable {
    public TodoListEventStoreEntityKey(String userId, String listId, int version) {
        this.userId = userId;
        this.listId = listId;
        this.version = version;
    }

    @Column(name = "user_id")
    public String userId;

    @Column(name = "list_id")
    public String listId;

    @Column(name = "version")
    public int version;
}
