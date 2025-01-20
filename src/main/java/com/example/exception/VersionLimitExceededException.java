package com.example.exception;

public class VersionLimitExceededException extends RuntimeException {
    public VersionLimitExceededException(String message) {
        super(message);
    }
} 