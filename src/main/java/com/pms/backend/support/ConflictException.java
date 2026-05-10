package com.pms.backend.support;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
