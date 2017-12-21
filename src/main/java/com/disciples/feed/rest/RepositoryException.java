package com.disciples.feed.rest;

public class RepositoryException extends RuntimeException {
    
    private static final long serialVersionUID = -3488122871077910418L;

    public RepositoryException() {}

    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepositoryException(Throwable cause) {
        super(cause);
    }

}
