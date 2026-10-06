package com.rhribeiro.poc.adapter.out.ai.ollama;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OllamaChatModelAdapterIntegrationTest {
    private static final AtomicInteger UPSTREAM_STATUS = new AtomicInteger(200);
    private static final AtomicReference<String> REQUEST = new AtomicReference<>();
    private static final AtomicReference<String> ANSWER = new AtomicReference<>("Hello Renan");
    private static final HttpServer OLLAMA = createServer();

    @Value("${local.server.port}")
    private int port;
    private RestClient client;

    private static HttpServer createServer() {
        try {
            var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/api/chat", exchange -> {
                REQUEST.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                String body = """
                        {"model":"qwen3:4b","message":{"role":"assistant","content":"%s"},
                         "done":true,"done_reason":"stop","prompt_eval_count":3,"eval_count":2}
                        """.formatted(ANSWER.get());
                if (UPSTREAM_STATUS.get() != 200) {
                    body = "{\"error\":\"Internal provider details\"}";
                }
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(UPSTREAM_STATUS.get(), bytes.length);
                try (var output = exchange.getResponseBody()) {
                    output.write(bytes);
                }
            });
            server.start();
            return server;
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    @DynamicPropertySource
    static void ollamaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.ai.ollama.base-url", () -> "http://127.0.0.1:" + OLLAMA.getAddress().getPort());
    }

    @BeforeEach
    void setUp() {
        ANSWER.set("Hello Renan");
        UPSTREAM_STATUS.set(200);
        REQUEST.set(null);
        client = RestClient.builder().baseUrl("http://127.0.0.1:" + port).build();
    }

    @AfterAll
    static void stopServer() {
        OLLAMA.stop(0);
    }

    @Test
    void sendsPromptThroughRealSpringAiClientAndReturnsAnswer() {
        String response = client.post().uri("/api/v1/chat").contentType(MediaType.APPLICATION_JSON)
                .body("{\"message\":\"Hello\"}").retrieve().body(String.class);
        assertThat(response).contains("\"answer\":\"Hello Renan\"");
        assertThat(REQUEST.get()).contains("Hello", "qwen3:4b", "\"stream\":false");
    }

    @Test
    void rejectsEmptyProviderResponse() {
        ANSWER.set("");
        assertThatThrownBy(() -> client.post().uri("/api/v1/chat").contentType(MediaType.APPLICATION_JSON)
                .body("{\"message\":\"Hello\"}").retrieve().body(String.class))
                .isInstanceOf(HttpServerErrorException.BadGateway.class);
    }

    @Test
    void translatesUpstreamFailureToBadGateway() {
        UPSTREAM_STATUS.set(503);
        assertThatThrownBy(() -> client.post().uri("/api/v1/chat").contentType(MediaType.APPLICATION_JSON)
                .body("{\"message\":\"Hello\"}").retrieve().body(String.class))
                .isInstanceOfSatisfying(HttpServerErrorException.BadGateway.class,
                        exception -> assertThat(exception.getResponseBodyAsString())
                                .contains("AI provider unavailable").doesNotContain("Internal provider details"));
    }

    @Test
    void exposesApplicationHealth() {
        assertThat(client.get().uri("/actuator/health").retrieve().body(String.class))
                .contains("\"status\":\"UP\"");
    }
}
