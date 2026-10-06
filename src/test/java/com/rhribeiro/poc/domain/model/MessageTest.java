package com.rhribeiro.poc.domain.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class MessageTest {
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n\t"})
    void rejectsEmptyMessages(String content) {
        assertThatIllegalArgumentException().isThrownBy(() -> new Message(content));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void rejectsEmptyResponses(String content) {
        assertThatIllegalArgumentException().isThrownBy(() -> new AiResponse(content));
    }
}
