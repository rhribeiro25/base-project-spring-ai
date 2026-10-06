package com.rhribeiro.poc.adapter.in.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotBlank(message = "message must not be blank")
        @Size(max = 16000, message = "message must have at most 16000 characters")
        String message) {
}
