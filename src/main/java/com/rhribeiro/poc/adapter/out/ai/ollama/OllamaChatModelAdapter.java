package com.rhribeiro.poc.adapter.out.ai.ollama;

import com.rhribeiro.poc.application.exception.AiProviderException;
import com.rhribeiro.poc.application.port.out.ChatModelPort;
import com.rhribeiro.poc.domain.model.AiResponse;
import com.rhribeiro.poc.domain.model.Message;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class OllamaChatModelAdapter implements ChatModelPort {
    private final ChatClient chatClient;

    public OllamaChatModelAdapter(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public AiResponse generate(Message message) {
        try {
            String content = chatClient.prompt().user(message.content()).call().content();
            return new AiResponse(content);
        } catch (RuntimeException exception) {
            throw new AiProviderException("AI provider could not generate a response", exception);
        }
    }
}
