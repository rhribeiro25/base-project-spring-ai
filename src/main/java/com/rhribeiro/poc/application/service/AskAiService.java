package com.rhribeiro.poc.application.service;

import com.rhribeiro.poc.application.port.in.AskAiUseCase;
import com.rhribeiro.poc.application.port.out.ChatModelPort;
import com.rhribeiro.poc.domain.model.AiResponse;
import com.rhribeiro.poc.domain.model.Message;
import java.util.Objects;

public class AskAiService implements AskAiUseCase {
    private final ChatModelPort chatModelPort;

    public AskAiService(ChatModelPort chatModelPort) {
        this.chatModelPort = Objects.requireNonNull(chatModelPort);
    }

    @Override
    public AiResponse ask(Message message) {
        return chatModelPort.generate(Objects.requireNonNull(message));
    }
}
