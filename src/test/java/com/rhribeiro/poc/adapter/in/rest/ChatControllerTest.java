package com.rhribeiro.poc.adapter.in.rest;

import com.rhribeiro.poc.adapter.in.rest.mapper.ChatRestMapper;
import com.rhribeiro.poc.application.exception.AiProviderException;
import com.rhribeiro.poc.application.port.in.AskAiUseCase;
import com.rhribeiro.poc.domain.model.AiResponse;
import com.rhribeiro.poc.domain.model.Message;
import com.rhribeiro.poc.infrastructure.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ChatControllerTest {
    private AskAiUseCase useCase;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        useCase = mock(AskAiUseCase.class);
        mvc = MockMvcBuilders.standaloneSetup(new ChatController(useCase, new ChatRestMapper()))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void returnsGeneratedAnswer() throws Exception {
        when(useCase.ask(new Message("Hello"))).thenReturn(new AiResponse("Hello Renan"));
        mvc.perform(post("/api/v1/chat").contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"Hello\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.answer").value("Hello Renan"));
        verify(useCase).ask(new Message("Hello"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"message\":null}", "{\"message\":\" \"}", "{", ""})
    void rejectsInvalidRequestsBeforeCallingUseCase(String json) throws Exception {
        mvc.perform(post("/api/v1/chat").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(useCase);
    }

    @Test
    void rejectsOversizedMessage() throws Exception {
        mvc.perform(post("/api/v1/chat").contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"" + "a".repeat(16001) + "\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(useCase);
    }

    @Test
    void hidesProviderFailureDetails() throws Exception {
        when(useCase.ask(any())).thenThrow(new AiProviderException("Sensitive upstream details", null));
        mvc.perform(post("/api/v1/chat").contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"Hello\"}"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("AI provider unavailable or returned an invalid response"))
                .andExpect(jsonPath("$.details").isEmpty());
    }
}
