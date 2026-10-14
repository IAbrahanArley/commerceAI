package br.com.commerceai.api.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(OutputCaptureExtension.class)
class AiConfigTests {

    @Test
    void doesNotLogApiKey(CapturedOutput output) throws Exception {
        String key = "test-only-not-a-real-key";
        try (var client = new AiConfig().googleGenAiClient(key, 60_000)) {
            assertThat(output.getAll())
                    .doesNotContain("Gemini API key carregada")
                    .doesNotContain(key);
        }
    }

    @Test
    void doesNotLogShortApiKey(CapturedOutput output) throws Exception {
        try (var client = new AiConfig().googleGenAiClient("abcd", 60_000)) {
            assertThat(output.getAll())
                    .doesNotContain("Gemini API key carregada")
                    .doesNotContain("abcd");
        }
    }

    @Test
    void rejectsUnresolvedPlaceholderWithoutLoggingIt(CapturedOutput output) {
        assertThatThrownBy(() -> new AiConfig().googleGenAiClient("${spring.ai.google.genai.api-key}", 60_000))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Configure GEMINI_API_KEY para iniciar a aplicacao.");
        assertThat(output.getOut()).doesNotContain("${spring.ai.google.genai.api-key}");
    }

    @Test
    void rejectsBlankKey(CapturedOutput output) {
        assertThatThrownBy(() -> new AiConfig().googleGenAiClient("   ", 60_000))
                .isInstanceOf(IllegalStateException.class);
        assertThat(output.getOut()).doesNotContain("Gemini API key carregada");
    }

    @Test
    void rejectsTimeoutThatWouldDisableTheLimit(CapturedOutput output) {
        assertThatThrownBy(() -> new AiConfig().googleGenAiClient("test-only-not-a-real-key", 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("GEMINI_TIMEOUT_MS deve ser maior que zero.");
        assertThat(output.getOut()).doesNotContain("Gemini API key carregada");
    }
}
