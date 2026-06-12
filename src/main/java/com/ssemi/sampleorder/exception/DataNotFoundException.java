package com.ssemi.sampleorder.exception;

public class DataNotFoundException extends IllegalArgumentException {

    public DataNotFoundException(String message) {
        super(message);
    }
}
