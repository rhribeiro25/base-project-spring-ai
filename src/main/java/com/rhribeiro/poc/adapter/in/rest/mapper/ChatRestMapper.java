package com.rhribeiro.poc.adapter.in.rest.mapper;

import com.rhribeiro.poc.adapter.in.rest.request.ChatRequest;
import com.rhribeiro.poc.adapter.in.rest.response.ChatResponse;
import com.rhribeiro.poc.domain.model.AiResponse;
import com.rhribeiro.poc.domain.model.Message;
import org.springframework.stereotype.Component;

@Component
public class ChatRestMapper {
    public Message toDomain(ChatRequest request) {
        return new Message(request.message());
    }

    public ChatResponse toResponse(AiResponse response) {
        return new ChatResponse(response.content());
    }
}
