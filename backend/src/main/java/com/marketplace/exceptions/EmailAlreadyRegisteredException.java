package com.marketplace.exceptions;

public class EmailAlreadyRegisteredException extends RuntimeException {
    public EmailAlreadyRegisteredException() {
        super("E-mail already registered");
    }

    public EmailAlreadyRegisteredException(Throwable cause) {
        super("E-mail already registered", cause);
    }
}
