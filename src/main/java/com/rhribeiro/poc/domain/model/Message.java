package com.rhribeiro.poc.domain.model;

public record Message(String content) {
    public Message {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }
    }
}
