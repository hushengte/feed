package com.disciples.feed.manage;

public class ManageException extends RuntimeException {
    
    private static final long serialVersionUID = -3488122871077910418L;

    public ManageException() {}

    public ManageException(String message) {
        super(message);
    }

    public ManageException(String message, Throwable cause) {
        super(message, cause);
    }

    public ManageException(Throwable cause) {
        super(cause);
    }

}
