package com.rhribeiro.poc.adapter.in.rest;

import com.rhribeiro.poc.adapter.in.rest.mapper.ChatRestMapper;
import com.rhribeiro.poc.adapter.in.rest.request.ChatRequest;
import com.rhribeiro.poc.adapter.in.rest.response.ChatResponse;
import com.rhribeiro.poc.application.port.in.AskAiUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {
    private final AskAiUseCase askAiUseCase;
    private final ChatRestMapper mapper;

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return mapper.toResponse(askAiUseCase.ask(mapper.toDomain(request)));
    }
}
