package br.com.commerceai.api.chat.memory;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TemporaryChatMemory {

    private final ChatMemoryProperties properties;
    private final Clock clock;
    private final Map<String, Session> sessions = new LinkedHashMap<>();

    public TemporaryChatMemory(ChatMemoryProperties properties, @Qualifier("chatMemoryClock") Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public <T> T withSession(String sessionId, Function<ChatMemory, T> action) {
        Session session = acquire(sessionId);
        session.lock.lock();
        try {
            return action.apply(session.memory);
        } finally {
            session.lock.unlock();
            synchronized (sessions) {
                session.lastActivity = clock.instant();
                session.inFlight--;
            }
        }
    }

    private Session acquire(String sessionId) {
        synchronized (sessions) {
            Instant now = clock.instant();
            removeExpired(now);
            Session session = sessions.get(sessionId);
            if (session == null) {
                if (sessions.size() >= properties.maxSessions()) {
                    String oldestIdleSession = sessions.entrySet().stream()
                            .filter(entry -> entry.getValue().inFlight == 0)
                            .min(Comparator.comparing(entry -> entry.getValue().lastActivity))
                            .map(Map.Entry::getKey)
                            .orElseThrow(ChatMemoryCapacityException::new);
                    sessions.remove(oldestIdleSession);
                }
                session = new Session(MessageWindowChatMemory.builder()
                        .maxMessages(properties.maxMessages()).build(), now);
                sessions.put(sessionId, session);
            }
            // Pin sessions while requests are executing or waiting for their session lock.
            session.inFlight++;
            return session;
        }
    }

    @Scheduled(fixedDelay = 60_000)
    public void cleanUpExpiredSessions() {
        synchronized (sessions) {
            removeExpired(clock.instant());
        }
    }

    private void removeExpired(Instant now) {
        sessions.values().removeIf(session -> session.inFlight == 0
                && !now.isBefore(session.lastActivity.plus(properties.ttl())));
    }

    private static class Session {

        private final ChatMemory memory;
        private final ReentrantLock lock = new ReentrantLock(true);
        private Instant lastActivity;
        private int inFlight;

        private Session(ChatMemory memory, Instant lastActivity) {
            this.memory = memory;
            this.lastActivity = lastActivity;
        }
    }
}
