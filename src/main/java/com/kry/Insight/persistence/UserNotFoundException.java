package com.kry.Insight.persistence;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String email) {
        super("email id: " + email + " was not found. ");
    }
}
