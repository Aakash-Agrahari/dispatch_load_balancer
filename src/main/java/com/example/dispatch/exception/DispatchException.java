package com.example.dispatch.exception;

/**
 * Thrown for domain-level errors that are the caller's fault (e.g. no
 * vehicles registered, no orders registered) and should map to HTTP 400.
 */
public class DispatchException extends RuntimeException {

    public DispatchException(String message) {
        super(message);
    }
}
