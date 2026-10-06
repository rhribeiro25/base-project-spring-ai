package com.rhribeiro.poc.application.service;

import com.rhribeiro.poc.application.exception.AiProviderException;
import com.rhribeiro.poc.application.port.out.ChatModelPort;
import com.rhribeiro.poc.domain.model.AiResponse;
import com.rhribeiro.poc.domain.model.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AskAiServiceTest {
    @Mock
    private ChatModelPort chatModelPort;

    @Test
    void delegatesToProviderIndependentPort() {
        var message = new Message("Hello");
        var response = new AiResponse("Hello Renan");
        when(chatModelPort.generate(message)).thenReturn(response);
        assertThat(new AskAiService(chatModelPort).ask(message)).isSameAs(response);
        verify(chatModelPort).generate(message);
    }

    @Test
    void propagatesProviderFailure() {
        var message = new Message("Hello");
        var failure = new AiProviderException("Unavailable", new RuntimeException());
        when(chatModelPort.generate(message)).thenThrow(failure);
        assertThatThrownBy(() -> new AskAiService(chatModelPort).ask(message)).isSameAs(failure);
    }
}
