package br.com.commerceai.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "GEMINI_API_KEY=test-only-not-a-real-key")
class AiConfigurationTests {

    @Autowired
    private GoogleGenAiChatModel model;

    @Test
    void configuresRealGeminiModelWithoutMakingExternalCalls() {
        assertThat(model.getOptions().getModel()).isEqualTo("gemini-2.5-flash");
        assertThat(model.getOptions().getTemperature()).isEqualTo(0.2);
        assertThat(model.getOptions().getMaxOutputTokens()).isEqualTo(1024);
    }
}
