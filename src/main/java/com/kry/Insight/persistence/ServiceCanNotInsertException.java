package com.kry.Insight.persistence;

import java.util.UUID;

public class ServiceCanNotInsertException extends RuntimeException {
    public ServiceCanNotInsertException(String url) {
        super("Service url: " + url + " could not insert");
    }
}
