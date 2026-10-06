package com.rhribeiro.poc.application.port.out;

import com.rhribeiro.poc.domain.model.AiResponse;
import com.rhribeiro.poc.domain.model.Message;

public interface ChatModelPort {
    AiResponse generate(Message message);
}
