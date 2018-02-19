package com.doerapispring.domain;

public class OperationRefusedException extends Exception {
    public OperationRefusedException(String message) {
        super(message);
    }

    public OperationRefusedException() {
    }
}
