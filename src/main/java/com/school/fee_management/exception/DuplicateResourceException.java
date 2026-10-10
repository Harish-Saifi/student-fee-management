package com.school.fee_management.exception;

public class DuplicateResourceException extends RuntimeException{
    public DuplicateResourceException(String msg) {
        super(msg);
    }
}
