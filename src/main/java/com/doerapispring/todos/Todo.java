package com.doerapispring.todos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chiragtailor on 9/27/16.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Todo {
    private String task;
    private boolean active;
}
