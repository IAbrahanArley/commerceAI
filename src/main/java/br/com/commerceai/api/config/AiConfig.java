package br.com.commerceai.api.config;

import com.google.genai.Client;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.HttpRetryOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class AiConfig {

    @Bean(destroyMethod = "close")
    public Client googleGenAiClient(@Value("${spring.ai.google.genai.api-key}") String apiKey,
            @Value("${commerce.ai.request-timeout-ms:60000}") int timeoutMs) {
        if (apiKey.isBlank() || apiKey.contains("${")) {
            throw new IllegalStateException("Configure GEMINI_API_KEY para iniciar a aplicacao.");
        }
        if (timeoutMs <= 0) {
            throw new IllegalStateException("GEMINI_TIMEOUT_MS deve ser maior que zero.");
        }
        return Client.builder()
                .apiKey(apiKey)
                .httpOptions(HttpOptions.builder()
                        .timeout(timeoutMs)
                        .retryOptions(HttpRetryOptions.builder().attempts(1).build())
                        .build())
                .build();
    }
}
