package com.kry.Insight.persistence;

import java.util.UUID;

public class ServiceNotFoundException extends RuntimeException {
    public ServiceNotFoundException(Long id) {
        super("Service id: " + id + " was not found. ");
    }
}
