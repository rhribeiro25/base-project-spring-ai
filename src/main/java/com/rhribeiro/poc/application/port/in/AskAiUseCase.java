package com.rhribeiro.poc.application.port.in;

import com.rhribeiro.poc.domain.model.AiResponse;
import com.rhribeiro.poc.domain.model.Message;

public interface AskAiUseCase {
    AiResponse ask(Message message);
}
