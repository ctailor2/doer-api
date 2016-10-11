package com.doerapispring;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chiragtailor on 9/30/16.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Error {
    private String status;
    private String message;
}
