package com.rhribeiro.poc.domain.model;

public record AiResponse(String content) {
    public AiResponse {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("AI response content cannot be empty");
        }
    }
}
