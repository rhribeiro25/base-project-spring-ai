package com.rhribeiro.poc.application.exception;

public class AiProviderException extends RuntimeException {
    public AiProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
