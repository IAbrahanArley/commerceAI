package br.com.commerceai.api.chat.memory;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("commerce.ai.memory")
public record ChatMemoryProperties(int maxMessages, int maxSessions, Duration ttl) {

    public ChatMemoryProperties {
        if (maxMessages < 2) {
            throw new IllegalArgumentException("commerce.ai.memory.max-messages deve ser pelo menos 2.");
        }
        if (maxSessions < 1) {
            throw new IllegalArgumentException("commerce.ai.memory.max-sessions deve ser positivo.");
        }
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("commerce.ai.memory.ttl deve ser positivo.");
        }
    }
}
