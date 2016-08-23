package com.doerapispring.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chiragtailor on 8/12/16.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserResponseWrapper {
    private UserEntity user;
}
