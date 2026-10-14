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
    public Client googleGenAiClient(@Value("${spring.ai.google.genai.api-key}") String apiKey) {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("Configure GEMINI_API_KEY para iniciar a aplicacao.");
        }
        return Client.builder()
                .apiKey(apiKey)
                .httpOptions(HttpOptions.builder()
                        .timeout(30_000)
                        .retryOptions(HttpRetryOptions.builder().attempts(1).build())
                        .build())
                .build();
    }
}
