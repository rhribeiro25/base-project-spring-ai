package com.rhribeiro.poc.infrastructure.config;

import com.rhribeiro.poc.application.port.in.AskAiUseCase;
import com.rhribeiro.poc.application.port.out.ChatModelPort;
import com.rhribeiro.poc.application.service.AskAiService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class BeanConfiguration {
    @Bean
    AskAiUseCase askAiUseCase(ChatModelPort chatModelPort) {
        return new AskAiService(chatModelPort);
    }
}
